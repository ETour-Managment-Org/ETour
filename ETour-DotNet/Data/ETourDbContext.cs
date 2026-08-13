using ETour.Api.Models;
using Microsoft.EntityFrameworkCore;

namespace ETour.Api.Data;

public class ETourDbContext : DbContext
{
    public ETourDbContext(DbContextOptions<ETourDbContext> options) : base(options) { }

    public DbSet<Role> Roles => Set<Role>();
    public DbSet<User> Users => Set<User>();
    public DbSet<Category> Categories => Set<Category>();
    public DbSet<SubCategoryMaster> SubCategories => Set<SubCategoryMaster>();
    public DbSet<Tour> Tours => Set<Tour>();
    public DbSet<TourCity> TourCities => Set<TourCity>();
    public DbSet<TourImages> TourImages => Set<TourImages>();
    public DbSet<Schedule> Schedules => Set<Schedule>();
    public DbSet<Cost> Costs => Set<Cost>();
    public DbSet<Itinerary> Itineraries => Set<Itinerary>();
    public DbSet<Journey> Journeys => Set<Journey>();
    public DbSet<Booking> Bookings => Set<Booking>();
    public DbSet<PassengerDetails> Passengers => Set<PassengerDetails>();
    public DbSet<Payment> Payments => Set<Payment>();
    public DbSet<Cancellation> Cancellations => Set<Cancellation>();
    public DbSet<Review> Reviews => Set<Review>();
    public DbSet<Feedback> Feedbacks => Set<Feedback>();
    public DbSet<Notification> Notifications => Set<Notification>();
    public DbSet<Ads> Ads => Set<Ads>();
    public DbSet<Language> Languages => Set<Language>();

    protected override void OnModelCreating(ModelBuilder b)
    {
        b.Entity<Role>(e =>
        {
            e.ToTable("role");
            e.HasKey(x => x.RoleId);
            e.Property(x => x.RoleId).HasColumnName("role_id");
            e.Property(x => x.RoleName).HasColumnName("role_name").HasMaxLength(50);
            e.Property(x => x.Description).HasColumnName("description").HasMaxLength(255);
        });

        b.Entity<User>(e =>
        {
            e.ToTable("users");
            e.HasKey(x => x.UserId);
            e.Property(x => x.UserId).HasColumnName("user_id");
            e.Property(x => x.RoleId).HasColumnName("role_id");
            e.Property(x => x.Username).HasColumnName("username").HasMaxLength(50);
            e.Property(x => x.PasswordHash).HasColumnName("password_hash").HasMaxLength(255);
            e.Property(x => x.Email).HasColumnName("email").HasMaxLength(100);
            e.Property(x => x.FirstName).HasColumnName("first_name").HasMaxLength(50);
            e.Property(x => x.LastName).HasColumnName("last_name").HasMaxLength(50);
            e.Property(x => x.PhoneNumber).HasColumnName("phone_number").HasMaxLength(20);
            e.Property(x => x.Address).HasColumnName("address").HasMaxLength(255);
            e.Property(x => x.City).HasColumnName("city").HasMaxLength(60);
            e.Property(x => x.Gender).HasColumnName("gender").HasMaxLength(10);
            e.Property(x => x.AuthProvider).HasColumnName("auth_provider").HasMaxLength(20);
            e.Property(x => x.ProviderId).HasColumnName("provider_id").HasMaxLength(100);
            e.Property(x => x.CreatedAt).HasColumnName("created_at");
            e.Property(x => x.Isactive).HasColumnName("isactive");
            e.HasOne(x => x.Role).WithMany(r => r.Users).HasForeignKey(x => x.RoleId);
        });

        b.Entity<Category>(e =>
        {
            e.ToTable("category");
            e.HasKey(x => x.CategoryId);
            e.Property(x => x.CategoryId).HasColumnName("category_id");
            e.Property(x => x.CatCode).HasColumnName("cat_code").HasMaxLength(10);
            e.Property(x => x.ParentId).HasColumnName("parent_id");
            e.Property(x => x.CategoryName).HasColumnName("category_name").HasMaxLength(100);
            e.Property(x => x.CatImagePath).HasColumnName("cat_image_path").HasMaxLength(255);
            e.Property(x => x.Flag).HasColumnName("flag");
            e.Property(x => x.Description).HasColumnName("description").HasMaxLength(255);
            e.Property(x => x.SuitableFor).HasColumnName("suitable_for").HasMaxLength(100);
            e.Property(x => x.Status).HasColumnName("status").HasMaxLength(20);
            e.Ignore(x => x.IsLeaf);
            e.Ignore(x => x.IsRoot);
            e.HasOne(x => x.Parent).WithMany(p => p.Children).HasForeignKey(x => x.ParentId);
        });

        b.Entity<SubCategoryMaster>(e =>
        {
            e.ToTable("sub_category_master");
            e.HasKey(x => x.SubcatId);
            e.Property(x => x.SubcatId).HasColumnName("subcat_id");
            e.Property(x => x.CatId).HasColumnName("cat_id");
            e.Property(x => x.SubcatName).HasColumnName("subcat_name").HasMaxLength(100);
            e.Property(x => x.SubcatImagePath).HasColumnName("subcat_image_path").HasMaxLength(255);
            e.Property(x => x.Flag).HasColumnName("flag");
            e.Property(x => x.Isactive).HasColumnName("isactive");
            e.HasOne(x => x.Category).WithMany(c => c.SubCategories).HasForeignKey(x => x.CatId);
        });

        b.Entity<Tour>(e =>
        {
            e.ToTable("tour");
            e.HasKey(x => x.TourId);
            e.Property(x => x.TourId).HasColumnName("tour_id");
            e.Property(x => x.TourName).HasColumnName("tour_name").HasMaxLength(150);
            e.Property(x => x.Destination).HasColumnName("destination").HasMaxLength(150);
            e.Property(x => x.Days).HasColumnName("days");
            e.Property(x => x.Nights).HasColumnName("nights");
            e.Property(x => x.Description).HasColumnName("description").HasMaxLength(1000);
            e.Property(x => x.Price).HasColumnName("price");
            e.Property(x => x.CategoryId).HasColumnName("category_id");
            e.Property(x => x.SubcatId).HasColumnName("subcat_id");
            e.Property(x => x.Location).HasColumnName("location").HasMaxLength(150);
            e.Property(x => x.TourType).HasColumnName("tour_type").HasMaxLength(20);
            e.Property(x => x.StayAndMeals).HasColumnName("stay_and_meals").HasColumnType("TEXT");
            e.Property(x => x.AddOns).HasColumnName("add_ons").HasColumnType("TEXT");
            e.Property(x => x.PassportAndVisa).HasColumnName("passport_and_visa").HasColumnType("TEXT");
            e.Property(x => x.Weather).HasColumnName("weather").HasColumnType("TEXT");
            e.Property(x => x.DoAndDont).HasColumnName("do_and_dont").HasColumnType("TEXT");
            e.HasOne(x => x.Category).WithMany(c => c.Tours).HasForeignKey(x => x.CategoryId);
            e.HasOne(x => x.SubCategory).WithMany(s => s.Tours).HasForeignKey(x => x.SubcatId);
        });

        b.Entity<TourCity>(e =>
        {
            e.ToTable("tour_city");
            e.HasKey(x => x.CityId);
            e.Property(x => x.CityId).HasColumnName("city_id");
            e.Property(x => x.TourId).HasColumnName("tour_id");
            e.Property(x => x.CityName).HasColumnName("city_name").HasMaxLength(100);
            e.Property(x => x.StopOrder).HasColumnName("stop_order");
            e.HasIndex(x => x.CityName).HasDatabaseName("idx_tour_city_name");
            e.HasOne(x => x.Tour).WithMany(t => t.Cities)
                .HasForeignKey(x => x.TourId).OnDelete(DeleteBehavior.Cascade);
        });

        b.Entity<TourImages>(e =>
        {
            e.ToTable("tour_images");
            e.HasKey(x => x.ImageId);
            e.Property(x => x.ImageId).HasColumnName("image_id");
            e.Property(x => x.TourId).HasColumnName("tour_id");
            e.Property(x => x.Source).HasColumnName("source").HasMaxLength(255);
            e.Property(x => x.ImageTitle).HasColumnName("image_title").HasMaxLength(150);
            e.Property(x => x.IsPrimary).HasColumnName("is_primary");
            e.Property(x => x.UploadDate).HasColumnName("upload_date");
            e.HasOne(x => x.Tour).WithMany(t => t.Images).HasForeignKey(x => x.TourId);
        });

        b.Entity<Schedule>(e =>
        {
            e.ToTable("schedule");
            e.HasKey(x => x.ScheduleId);
            e.Property(x => x.ScheduleId).HasColumnName("schedule_id");
            e.Property(x => x.TourId).HasColumnName("tour_id");
            e.Property(x => x.AvailableSeats).HasColumnName("available_seats");
            e.Property(x => x.TotalSeats).HasColumnName("total_seats");
            e.Property(x => x.Status).HasColumnName("status").HasMaxLength(20);
            e.Property(x => x.StartDate).HasColumnName("start_date");
            e.HasOne(x => x.Tour).WithMany(t => t.Schedules).HasForeignKey(x => x.TourId);
        });

        b.Entity<Cost>(e =>
        {
            e.ToTable("cost");
            e.HasKey(x => x.CostId);
            e.Property(x => x.CostId).HasColumnName("cost_id");
            e.Property(x => x.TourId).HasColumnName("tour_id");
            e.Property(x => x.AdultPrice).HasColumnName("adult_price").HasPrecision(10, 2);
            e.Property(x => x.SinglePersonPrice).HasColumnName("single_person_price").HasPrecision(10, 2);
            e.Property(x => x.ExtraPersonPrice).HasColumnName("extra_person_price").HasPrecision(10, 2);
            e.Property(x => x.ChildWithBedPrice).HasColumnName("child_with_bed_price").HasPrecision(10, 2);
            e.Property(x => x.ChildWithoutBedPrice).HasColumnName("child_without_bed_price").HasPrecision(10, 2);
            e.Property(x => x.ValidFrom).HasColumnName("valid_from");
            e.Property(x => x.ValidTo).HasColumnName("valid_to");
            e.Property(x => x.IsActive).HasColumnName("is_active");
            e.HasOne(x => x.Tour).WithMany(t => t.Costs).HasForeignKey(x => x.TourId);
        });

        b.Entity<Itinerary>(e =>
        {
            e.ToTable("itinerary");
            e.HasKey(x => x.ItineraryId);
            e.Property(x => x.ItineraryId).HasColumnName("itinerary_id");
            e.Property(x => x.TourId).HasColumnName("tour_id");
            e.Property(x => x.DayNumber).HasColumnName("day_number");
            e.Property(x => x.Description).HasColumnName("description").HasMaxLength(1000);
            e.Property(x => x.Location).HasColumnName("location").HasMaxLength(150);
            e.HasOne(x => x.Tour).WithMany(t => t.Itineraries).HasForeignKey(x => x.TourId);
        });

        b.Entity<Journey>(e =>
        {
            e.ToTable("journey");
            e.HasKey(x => x.JourneyId);
            e.Property(x => x.JourneyId).HasColumnName("journey_id");
            e.Property(x => x.TourId).HasColumnName("tour_id");
            e.Property(x => x.Source).HasColumnName("source").HasMaxLength(150);
            e.Property(x => x.Destination).HasColumnName("destination").HasMaxLength(150);
            e.Property(x => x.Transport).HasColumnName("transport").HasMaxLength(100);
            e.Property(x => x.Details).HasColumnName("details").HasColumnType("TEXT");
            e.HasOne(x => x.Tour).WithMany(t => t.Journeys).HasForeignKey(x => x.TourId);
        });

        b.Entity<Booking>(e =>
        {
            e.ToTable("booking");
            e.HasKey(x => x.BookingId);
            e.Property(x => x.BookingId).HasColumnName("booking_id");
            e.Property(x => x.UserId).HasColumnName("user_id");
            e.Property(x => x.TourId).HasColumnName("tour_id");
            e.Property(x => x.ScheduleId).HasColumnName("schedule_id");
            e.Property(x => x.BookingDate).HasColumnName("booking_date");
            e.Property(x => x.TotalAmount).HasColumnName("total_amount").HasPrecision(12, 2);
            e.Property(x => x.BookingStatus).HasColumnName("booking_status").HasMaxLength(30);
            e.Property(x => x.ContactName).HasColumnName("contact_name").HasMaxLength(120);
            e.Property(x => x.ContactEmail).HasColumnName("contact_email").HasMaxLength(150);
            e.Property(x => x.ContactPhone).HasColumnName("contact_phone").HasMaxLength(15);
            e.HasOne(x => x.User).WithMany(u => u.Bookings).HasForeignKey(x => x.UserId);
            e.HasOne(x => x.Tour).WithMany().HasForeignKey(x => x.TourId);
            e.HasOne(x => x.Schedule).WithMany().HasForeignKey(x => x.ScheduleId);
        });

        b.Entity<PassengerDetails>(e =>
        {
            e.ToTable("passenger_details");
            e.HasKey(x => x.PaxId);
            e.Property(x => x.PaxId).HasColumnName("pax_id");
            e.Property(x => x.BookingId).HasColumnName("booking_id");
            e.Property(x => x.FullName).HasColumnName("full_name").HasMaxLength(120);
            e.Property(x => x.Email).HasColumnName("email").HasMaxLength(100);
            e.Property(x => x.Gender).HasColumnName("gender").HasMaxLength(10);
            e.Property(x => x.BirthDate).HasColumnName("birth_date");
            e.Property(x => x.Age).HasColumnName("age");
            e.Property(x => x.PassportNumber).HasColumnName("passport_number").HasMaxLength(30);
            e.Property(x => x.PaxType).HasColumnName("pax_type").HasMaxLength(20);
            e.HasOne(x => x.Booking).WithMany(bk => bk.Passengers)
                .HasForeignKey(x => x.BookingId).OnDelete(DeleteBehavior.Cascade);
        });

        b.Entity<Payment>(e =>
        {
            e.ToTable("payment");
            e.HasKey(x => x.PaymentId);
            e.Property(x => x.PaymentId).HasColumnName("payment_id");
            e.Property(x => x.BookingId).HasColumnName("booking_id");
            e.Property(x => x.Amount).HasColumnName("amount").HasPrecision(12, 2);
            e.Property(x => x.PaymentDate).HasColumnName("payment_date");
            e.Property(x => x.PaymentStatus).HasColumnName("payment_status").HasMaxLength(30);
            e.Property(x => x.PaymentMethod).HasColumnName("payment_method").HasMaxLength(30);
            e.Property(x => x.TransactionRef).HasColumnName("transaction_ref").HasMaxLength(64);
            e.HasOne(x => x.Booking).WithMany(bk => bk.Payments).HasForeignKey(x => x.BookingId);
        });

        b.Entity<Cancellation>(e =>
        {
            e.ToTable("cancellation");
            e.HasKey(x => x.CancellationId);
            e.Property(x => x.CancellationId).HasColumnName("cancellation_id");
            e.Property(x => x.BookingId).HasColumnName("booking_id");
            e.Property(x => x.CancellationDate).HasColumnName("cancellation_date");
            e.Property(x => x.Reason).HasColumnName("reason").HasMaxLength(255);
            e.Property(x => x.RefundAmount).HasColumnName("refund_amount").HasPrecision(12, 2);
            e.Property(x => x.RefundStatus).HasColumnName("refund_status").HasMaxLength(30);
            e.Property(x => x.Remarks).HasColumnName("remarks").HasMaxLength(255);
            e.HasOne(x => x.Booking).WithOne(bk => bk.Cancellation).HasForeignKey<Cancellation>(x => x.BookingId);
        });

        b.Entity<Review>(e =>
        {
            e.ToTable("review");
            e.HasKey(x => x.ReviewId);
            e.Property(x => x.ReviewId).HasColumnName("review_id");
            e.Property(x => x.BookingId).HasColumnName("booking_id");
            e.Property(x => x.CustomerId).HasColumnName("customer_id");
            e.Property(x => x.TourId).HasColumnName("tour_id");
            e.Property(x => x.Rating).HasColumnName("rating");
            e.Property(x => x.ReviewTitle).HasColumnName("review_title").HasMaxLength(150);
            e.Property(x => x.ReviewDescription).HasColumnName("review_description").HasMaxLength(1000);
            e.Property(x => x.ReviewDate).HasColumnName("review_date");
            e.Property(x => x.VerificationStatus).HasColumnName("verification_status").HasMaxLength(30);
            e.HasOne(x => x.Booking).WithMany(bk => bk.Reviews).HasForeignKey(x => x.BookingId);
            e.HasOne(x => x.Customer).WithMany(u => u.Reviews).HasForeignKey(x => x.CustomerId);
            e.HasOne(x => x.Tour).WithMany(t => t.Reviews).HasForeignKey(x => x.TourId);
        });

        b.Entity<Feedback>(e =>
        {
            e.ToTable("feedback");
            e.HasKey(x => x.FeedbackId);
            e.Property(x => x.FeedbackId).HasColumnName("feedback_id");
            e.Property(x => x.UserId).HasColumnName("user_id");
            e.Property(x => x.Name).HasColumnName("name").HasMaxLength(120);
            e.Property(x => x.Email).HasColumnName("email").HasMaxLength(120);
            e.Property(x => x.Category).HasColumnName("category").HasMaxLength(30);
            e.Property(x => x.Rating).HasColumnName("rating");
            e.Property(x => x.Message).HasColumnName("message").HasMaxLength(2000);
            e.Property(x => x.Status).HasColumnName("status").HasMaxLength(20);
            e.Property(x => x.PageUrl).HasColumnName("page_url").HasMaxLength(255);
            e.Property(x => x.Published).HasColumnName("published");
            e.Property(x => x.CreatedAt).HasColumnName("created_at");
            e.HasOne(x => x.User).WithMany().HasForeignKey(x => x.UserId);
        });

        b.Entity<Notification>(e =>
        {
            e.ToTable("notification");
            e.HasKey(x => x.NotificationId);
            e.Property(x => x.NotificationId).HasColumnName("notification_id");
            e.Property(x => x.UserId).HasColumnName("user_id");
            e.Property(x => x.Title).HasColumnName("title").HasMaxLength(150);
            e.Property(x => x.Message).HasColumnName("message").HasMaxLength(500);
            e.Property(x => x.IsRead).HasColumnName("is_read");
            e.Property(x => x.CreatedDate).HasColumnName("created_date");
            e.HasOne(x => x.User).WithMany(u => u.Notifications).HasForeignKey(x => x.UserId);
        });

        b.Entity<Ads>(e =>
        {
            e.ToTable("ads");
            e.HasKey(x => x.AdId);
            e.Property(x => x.AdId).HasColumnName("ad_id");
            e.Property(x => x.Title).HasColumnName("title").HasMaxLength(150);
            e.Property(x => x.ImagePath).HasColumnName("image_path").HasMaxLength(255);
            e.Property(x => x.LinkUrl).HasColumnName("link_url").HasMaxLength(255);
            e.Property(x => x.StartDate).HasColumnName("start_date");
            e.Property(x => x.EndDate).HasColumnName("end_date");
            e.Property(x => x.DisplayOrder).HasColumnName("display_order");
            e.Property(x => x.Active).HasColumnName("active");
        });

        b.Entity<Language>(e =>
        {
            e.ToTable("language");
            e.HasKey(x => x.LanguageId);
            e.Property(x => x.LanguageId).HasColumnName("language_id");
            e.Property(x => x.LanguageCode).HasColumnName("language_code").HasMaxLength(10);
            e.Property(x => x.LanguageName).HasColumnName("language_name").HasMaxLength(60);
        });
    }
}
