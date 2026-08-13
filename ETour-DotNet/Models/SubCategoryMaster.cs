namespace ETour.Api.Models;

public class SubCategoryMaster
{
    public int SubcatId { get; set; }

    public int? CatId { get; set; }
    public Category Category { get; set; }

    public string SubcatName { get; set; }
    public string SubcatImagePath { get; set; }
    public bool? Flag { get; set; }
    public bool? Isactive { get; set; }

    public ICollection<Tour> Tours { get; set; } = new List<Tour>();
}
