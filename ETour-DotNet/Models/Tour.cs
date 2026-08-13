namespace ETour.Api.Models;

public class Tour
{
    public int TourId { get; set; }
    public string TourName { get; set; }
    public string Destination { get; set; }
    public int? Days { get; set; }
    public int? Nights { get; set; }
    public string Description { get; set; }
    public float? Price { get; set; }

    public int? CategoryId { get; set; }
    public Category Category { get; set; }

    public int? SubcatId { get; set; }
    public SubCategoryMaster SubCategory { get; set; }

    public string Location { get; set; }
    public string TourType { get; set; }

    public string StayAndMeals { get; set; }
    public string AddOns { get; set; }
    public string PassportAndVisa { get; set; }
    public string Weather { get; set; }
    public string DoAndDont { get; set; }

    public ICollection<Schedule> Schedules { get; set; } = new List<Schedule>();
    public ICollection<Itinerary> Itineraries { get; set; } = new List<Itinerary>();
    public ICollection<Journey> Journeys { get; set; } = new List<Journey>();
    public ICollection<TourImages> Images { get; set; } = new List<TourImages>();
    public ICollection<Cost> Costs { get; set; } = new List<Cost>();
    public ICollection<TourCity> Cities { get; set; } = new List<TourCity>();
    public ICollection<Review> Reviews { get; set; } = new List<Review>();
}
