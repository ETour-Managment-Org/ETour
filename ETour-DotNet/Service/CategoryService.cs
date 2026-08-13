using ETour.Api.Common;
using ETour.Api.Data;
using ETour.Api.DTO;
using ETour.Api.Models;
using ETour.Api.Exceptions;
using Microsoft.EntityFrameworkCore;

namespace ETour.Api.Service;

public class CategoryService : ICategoryService
{
    private const string ActionShowTours = "SHOW_TOURS";
    private const string ActionShowCategories = "SHOW_CATEGORIES";
    private const int MaxTreeDepth = 20;

    private readonly ETourDbContext _db;

    public CategoryService(ETourDbContext db) => _db = db;

    public async Task<List<CategoryDTO>> GetRootCategoriesAsync()
    {
        var roots = await _db.Categories.AsNoTracking()
            .Where(c => c.ParentId == null).OrderBy(c => c.CategoryId).ToListAsync();
        return await DecorateAsync(roots);
    }

    public async Task<List<CategoryDTO>> GetChildrenAsync(int categoryId)
    {
        if (!await _db.Categories.AnyAsync(c => c.CategoryId == categoryId))
            throw new ResourceNotFoundException("Category", categoryId);

        var kids = await _db.Categories.AsNoTracking()
            .Where(c => c.ParentId == categoryId).OrderBy(c => c.CategoryId).ToListAsync();
        return await DecorateAsync(kids);
    }

    public async Task<List<BreadcrumbDTO>> GetBreadcrumbAsync(int categoryId)
    {
        var all = await _db.Categories.AsNoTracking().ToListAsync();
        var byId = all.ToDictionary(c => c.CategoryId);

        if (!byId.TryGetValue(categoryId, out var node))
            throw new ResourceNotFoundException("Category", categoryId);

        var trail = new List<BreadcrumbDTO>();
        var guard = 0;

        while (node is not null && guard++ < MaxTreeDepth)
        {
            trail.Add(new BreadcrumbDTO
            {
                CategoryId = node.CategoryId,
                CatCode = node.CatCode,
                CategoryName = node.CategoryName
            });
            node = node.ParentId is int pid && byId.TryGetValue(pid, out var p) ? p : null;
        }

        trail.Reverse();
        for (var i = 0; i < trail.Count; i++) trail[i].Level = i + 1;
        return trail;
    }

    public async Task<List<CategoryDTO>> GetAllCategoriesAsync() =>
        await DecorateAsync(await _db.Categories.AsNoTracking().OrderBy(c => c.CategoryId).ToListAsync());

    public async Task<CategoryDTO> GetCategoryAsync(int categoryId)
    {
        var category = await _db.Categories.AsNoTracking()
            .FirstOrDefaultAsync(c => c.CategoryId == categoryId)
            ?? throw new ResourceNotFoundException("Category", categoryId);

        return (await DecorateAsync(new List<Category> { category }))[0];
    }

    public async Task<CategoryDTO> GetCategoryByCodeAsync(string catCode)
    {
        var category = await _db.Categories.AsNoTracking()
            .FirstOrDefaultAsync(c => c.CatCode == catCode)
            ?? throw new ResourceNotFoundException($"Category not found with code {catCode}");

        return (await DecorateAsync(new List<Category> { category }))[0];
    }

    public async Task<List<SubCategoryDTO>> GetSubCategoriesAsync(int categoryId)
    {
        if (!await _db.Categories.AnyAsync(c => c.CategoryId == categoryId))
            throw new ResourceNotFoundException("Category", categoryId);

        var counts = await TourCountBySubCategoryAsync();

        return await _db.SubCategories.AsNoTracking()
            .Include(s => s.Category)
            .Where(s => s.CatId == categoryId && s.Isactive == true)
            .Select(s => new SubCategoryDTO
            {
                SubcatId = s.SubcatId,
                SubcatName = s.SubcatName,
                SubcatImagePath = s.SubcatImagePath,
                CategoryId = s.CatId,
                CategoryName = s.Category.CategoryName,
                TourCount = counts.GetValueOrDefault(s.SubcatId, 0L)
            }).ToListAsync();
    }

    public async Task<List<SubCategoryDTO>> GetAllSubCategoriesAsync()
    {
        var counts = await TourCountBySubCategoryAsync();

        return await _db.SubCategories.AsNoTracking()
            .Include(s => s.Category)
            .Select(s => new SubCategoryDTO
            {
                SubcatId = s.SubcatId,
                SubcatName = s.SubcatName,
                SubcatImagePath = s.SubcatImagePath,
                CategoryId = s.CatId,
                CategoryName = s.Category.CategoryName,
                TourCount = counts.GetValueOrDefault(s.SubcatId, 0L)
            }).ToListAsync();
    }

    private async Task<List<CategoryDTO>> DecorateAsync(List<Category> categories)
    {
        var all = await _db.Categories.AsNoTracking().ToListAsync();
        var tours = await _db.Tours.AsNoTracking()
            .Select(t => new { t.TourId, t.CategoryId }).ToListAsync();

        var byId = all.ToDictionary(c => c.CategoryId);

        var childCounts = all
            .Where(c => c.ParentId != null)
            .GroupBy(c => c.ParentId.Value)
            .ToDictionary(g => g.Key, g => (long)g.Count());

        var tourCounts = new Dictionary<int, long>();
        foreach (var c in all)
        {
            var scope = CategoryScope.Resolve(c.CategoryId, all);
            tourCounts[c.CategoryId] =
                tours.Count(t => t.CategoryId != null && scope.Contains(t.CategoryId.Value));
        }

        return categories.Select(c =>
        {
            var children = childCounts.GetValueOrDefault(c.CategoryId, 0L);
            var leaf = c.Flag == true || children == 0;

            return new CategoryDTO
            {
                CategoryId = c.CategoryId,
                CatCode = c.CatCode,
                CategoryName = c.CategoryName,
                CatImagePath = c.CatImagePath,
                Description = c.Description,
                SuitableFor = c.SuitableFor,
                Status = c.Status,
                ParentId = c.ParentId,
                ParentName = c.ParentId is int p && byId.TryGetValue(p, out var parent)
                    ? parent.CategoryName : null,
                Flag = c.Flag == true,
                NextAction = leaf ? ActionShowTours : ActionShowCategories,
                ChildCount = children,
                TourCount = tourCounts.GetValueOrDefault(c.CategoryId, 0L)
            };
        }).ToList();
    }

    private async Task<Dictionary<int, long>> TourCountBySubCategoryAsync() =>
        await _db.Tours.AsNoTracking()
            .Where(t => t.SubcatId != null)
            .GroupBy(t => t.SubcatId.Value)
            .Select(g => new { SubcatId = g.Key, Count = (long)g.Count() })
            .ToDictionaryAsync(x => x.SubcatId, x => x.Count);
}
