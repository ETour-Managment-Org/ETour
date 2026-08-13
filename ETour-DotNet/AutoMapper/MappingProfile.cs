using AutoMapper;
using ETour.Api.DTO;
using ETour.Api.Models;

namespace ETour.Api.Mapping;

public class MappingProfile : Profile
{
    public MappingProfile()
    {
        CreateMap<Cost, CostDTO>();
        CreateMap<Itinerary, ItineraryDTO>();
        CreateMap<Schedule, ScheduleDTO>();
        CreateMap<TourImages, TourImageDTO>();

        CreateMap<Tour, TourDetailDTO>()
            .ForMember(d => d.CategoryName, o => o.MapFrom(s => s.Category.CategoryName))
            .ForMember(d => d.SubCategoryId, o => o.MapFrom(s => s.SubcatId))
            .ForMember(d => d.SubCategoryName, o => o.MapFrom(s => s.SubCategory.SubcatName))
            .ForMember(d => d.DurationLabel, o => o.Ignore())
            .ForMember(d => d.AverageRating, o => o.Ignore())
            .ForMember(d => d.ReviewCount, o => o.Ignore())
            .ForMember(d => d.PrimaryImageUrl, o => o.Ignore())
            .ForMember(d => d.Itineraries, o => o.Ignore())
            .ForMember(d => d.Schedules, o => o.Ignore())
            .ForMember(d => d.Costs, o => o.Ignore())
            .ForMember(d => d.Images, o => o.Ignore());

        CreateMap<Tour, TourListDTO>()
            .ForMember(d => d.StartingPrice, o => o.Ignore())
            .ForMember(d => d.PrimaryImageUrl, o => o.Ignore())
            .ForMember(d => d.DurationLabel, o => o.Ignore())
            .ForMember(d => d.AverageRating, o => o.Ignore())
            .ForMember(d => d.ReviewCount, o => o.Ignore());

        CreateMap<Category, CategoryDTO>()
            .ForMember(d => d.ParentName, o => o.MapFrom(s => s.Parent.CategoryName))
            .ForMember(d => d.NextAction, o => o.Ignore())
            .ForMember(d => d.ChildCount, o => o.Ignore())
            .ForMember(d => d.TourCount, o => o.Ignore());

        CreateMap<Category, BreadcrumbDTO>()
            .ForMember(d => d.Level, o => o.Ignore());

        CreateMap<SubCategoryMaster, SubCategoryDTO>()
            .ForMember(d => d.CategoryId, o => o.MapFrom(s => s.CatId))
            .ForMember(d => d.CategoryName, o => o.MapFrom(s => s.Category.CategoryName))
            .ForMember(d => d.TourCount, o => o.Ignore());

        CreateMap<User, UserProfileDTO>()
            .ForMember(d => d.FullName, o => o.MapFrom(s => s.DisplayName()))
            .ForMember(d => d.Role, o => o.MapFrom(s => s.Role.RoleName));

        CreateMap<UpdateProfileRequest, User>()
            .ForAllMembers(o => o.Condition((_, _, value) => value is not null));

        CreateMap<PassengerDetails, PassengerResponseDTO>()
            .ForMember(d => d.AgeAtDeparture, o => o.MapFrom(s => s.Age))
            .ForMember(d => d.PaxTypeLabel, o => o.Ignore())
            .ForMember(d => d.PaxAmount, o => o.Ignore());

        CreateMap<TourRequestDTO, Tour>()
            .ForMember(d => d.SubcatId, o => o.MapFrom(s => s.SubCategoryId))
            .ForMember(d => d.TourId, o => o.Ignore())
            .ForMember(d => d.Category, o => o.Ignore())
            .ForMember(d => d.SubCategory, o => o.Ignore())
            .ForMember(d => d.Schedules, o => o.Ignore())
            .ForMember(d => d.Itineraries, o => o.Ignore())
            .ForMember(d => d.Journeys, o => o.Ignore())
            .ForMember(d => d.Images, o => o.Ignore())
            .ForMember(d => d.Costs, o => o.Ignore())
            .ForMember(d => d.Cities, o => o.Ignore())
            .ForMember(d => d.Reviews, o => o.Ignore());

        CreateMap<CostRequestDTO, Cost>()
            .ForMember(d => d.CostId, o => o.Ignore())
            .ForMember(d => d.Tour, o => o.Ignore());


        CreateMap<ItineraryRequestDTO, Itinerary>()
            .ForMember(d => d.ItineraryId, o => o.Ignore())
            .ForMember(d => d.Tour, o => o.Ignore());

        CreateMap<ScheduleRequestDTO, Schedule>()
            .ForMember(d => d.ScheduleId, o => o.Ignore())
            .ForMember(d => d.Tour, o => o.Ignore());

        CreateMap<CostCreateDTO, Cost>()
            .ForMember(d => d.CostId, o => o.Ignore())
            .ForMember(d => d.Tour, o => o.Ignore())
            .ForMember(d => d.TourId, o => o.Ignore());

        CreateMap<ScheduleCreateDTO, Schedule>()
            .ForMember(d => d.ScheduleId, o => o.Ignore())
            .ForMember(d => d.Tour, o => o.Ignore())
            .ForMember(d => d.TourId, o => o.Ignore());

        CreateMap<ItineraryCreateDTO, Itinerary>()
            .ForMember(d => d.ItineraryId, o => o.Ignore())
            .ForMember(d => d.Tour, o => o.Ignore())
            .ForMember(d => d.TourId, o => o.Ignore());

        CreateMap<TourImageCreateDTO, TourImages>()
            .ForMember(d => d.ImageId, o => o.Ignore())
            .ForMember(d => d.Tour, o => o.Ignore())
            .ForMember(d => d.TourId, o => o.Ignore())
            .ForMember(d => d.UploadDate, o => o.Ignore());

        CreateMap<Feedback, FeedbackResponseDTO>()
            .ForMember(d => d.Username, o => o.MapFrom(s => s.User.Username))
            .ForMember(d => d.FromRegisteredUser, o => o.MapFrom(s => s.UserId != null))
            .ForMember(d => d.Published, o => o.MapFrom(s => s.Published == true));

        CreateMap<Feedback, PublicFeedbackDTO>()
            .ForMember(d => d.SubmittedOn, o => o.MapFrom(
                s => s.CreatedAt == null ? (DateOnly?)null : DateOnly.FromDateTime(s.CreatedAt.Value)))
            .ForMember(d => d.FromRegisteredUser, o => o.MapFrom(s => s.UserId != null));

        CreateMap<Review, ReviewResponseDTO>()
            .ForMember(d => d.TourName, o => o.MapFrom(s => s.Tour.TourName))
            .ForMember(d => d.CustomerName, o => o.MapFrom(s => s.Customer.DisplayName()));
    }
}
