namespace ETour.Api.Models;

public class Category
{
    public int CategoryId { get; set; }
    public string CatCode { get; set; }

    public int? ParentId { get; set; }
    public Category Parent { get; set; }
    public ICollection<Category> Children { get; set; } = new List<Category>();

    public string CategoryName { get; set; }
    public string CatImagePath { get; set; }
    public bool? Flag { get; set; }
    public string Description { get; set; }
    public string SuitableFor { get; set; }
    public string Status { get; set; }

    public ICollection<SubCategoryMaster> SubCategories { get; set; } = new List<SubCategoryMaster>();
    public ICollection<Tour> Tours { get; set; } = new List<Tour>();

    public bool IsLeaf => Flag == true;
    public bool IsRoot => ParentId is null;
}
