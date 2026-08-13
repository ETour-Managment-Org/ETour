namespace ETour.Api.DTO;

public class CategoryDTO
{
    public int? CategoryId { get; set; }
    public string CatCode { get; set; }
    public string CategoryName { get; set; }
    public string CatImagePath { get; set; }
    public string Description { get; set; }
    public string SuitableFor { get; set; }
    public string Status { get; set; }
    public int? ParentId { get; set; }
    public string ParentName { get; set; }
    public bool? Flag { get; set; }
    public string NextAction { get; set; }
    public long? ChildCount { get; set; }
    public long? TourCount { get; set; }
}

public class BreadcrumbDTO
{
    public int? CategoryId { get; set; }
    public string CatCode { get; set; }
    public string CategoryName { get; set; }
    public int? Level { get; set; }
}

public class SubCategoryDTO
{
    public int? SubcatId { get; set; }
    public string SubcatName { get; set; }
    public string SubcatImagePath { get; set; }
    public int? CategoryId { get; set; }
    public string CategoryName { get; set; }
    public long? TourCount { get; set; }
}
