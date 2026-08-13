using AutoMapper;
using ETour.Api.Common;
using ETour.Api.Data;
using ETour.Api.DTO;
using ETour.Api.Models;
using ETour.Api.Exceptions;
using Microsoft.EntityFrameworkCore;

namespace ETour.Api.Service;

public class FeedbackService : IFeedbackService
{
    private readonly ETourDbContext _db;
    private readonly IMapper _mapper;
    private readonly ILogger<FeedbackService> _log;

    public FeedbackService(ETourDbContext db, IMapper mapper, ILogger<FeedbackService> log)
    {
        _db = db;
        _mapper = mapper;
        _log = log;
    }

    public async Task<FeedbackResponseDTO> SubmitAsync(FeedbackRequestDTO request, string username)
    {
        if (!FeedbackCategory.IsValidCategory(request.Category))
            throw new ArgumentException(
                $"category must be one of {string.Join(", ", FeedbackCategory.All)}");

        var user = string.IsNullOrWhiteSpace(username)
            ? null
            : await _db.Users.FirstOrDefaultAsync(u => u.Username == username);

        var name = request.Name;
        var email = request.Email;

        if (user is not null)
        {
            name = user.DisplayName();
            email = user.Email;
        }

        var feedback = new Feedback
        {
            UserId = user?.UserId,
            Name = name,
            Email = email,
            Category = request.Category.Trim().ToUpperInvariant(),
            Rating = request.Rating,
            Message = request.Message,
            PageUrl = request.PageUrl,
            Status = FeedbackCategory.New,
            Published = false,
            CreatedAt = DateTime.Now
        };

        _db.Feedbacks.Add(feedback);
        await _db.SaveChangesAsync();

        _log.LogInformation("Website feedback #{Id} received: {Category} from {Who}",
            feedback.FeedbackId, feedback.Category, user?.Username ?? "a guest");

        feedback.User = user;
        return _mapper.Map<FeedbackResponseDTO>(feedback);
    }

    public async Task<List<FeedbackResponseDTO>> GetAllAsync(string status)
    {
        var query = _db.Feedbacks.AsNoTracking().Include(f => f.User).AsQueryable();

        if (!string.IsNullOrWhiteSpace(status)
            && !string.Equals(status, "ALL", StringComparison.OrdinalIgnoreCase))
        {
            var upper = status.ToUpperInvariant();
            query = query.Where(f => f.Status == upper);
        }

        var rows = await query.OrderByDescending(f => f.FeedbackId).ToListAsync();
        return rows.Select(_mapper.Map<FeedbackResponseDTO>).ToList();
    }

    public async Task<FeedbackResponseDTO> UpdateStatusAsync(int feedbackId, string status)
    {
        var feedback = await _db.Feedbacks.Include(f => f.User)
            .FirstOrDefaultAsync(f => f.FeedbackId == feedbackId)
            ?? throw new ResourceNotFoundException("Feedback", feedbackId);

        feedback.Status = status is null ? FeedbackCategory.New : status.ToUpperInvariant();
        await _db.SaveChangesAsync();
        return _mapper.Map<FeedbackResponseDTO>(feedback);
    }

    public async Task<FeedbackResponseDTO> SetPublishedAsync(int feedbackId, bool published)
    {
        var feedback = await _db.Feedbacks.Include(f => f.User)
            .FirstOrDefaultAsync(f => f.FeedbackId == feedbackId)
            ?? throw new ResourceNotFoundException("Feedback", feedbackId);

        feedback.Published = published;

        if (published && feedback.Status == FeedbackCategory.New)
        {
            feedback.Status = FeedbackCategory.Reviewed;
        }

        await _db.SaveChangesAsync();

        _log.LogInformation("Feedback #{Id} {Action} the public site",
            feedbackId, published ? "published to" : "removed from");

        return _mapper.Map<FeedbackResponseDTO>(feedback);
    }

    public async Task<List<PublicFeedbackDTO>> GetPublishedAsync()
    {
        var rows = await _db.Feedbacks.AsNoTracking()
            .Where(f => f.Published == true)
            .OrderByDescending(f => f.FeedbackId)
            .ToListAsync();

        return rows.Select(f => new PublicFeedbackDTO
        {
            FeedbackId = f.FeedbackId,
            Name = ShortenName(f.Name),
            Category = f.Category,
            Rating = f.Rating,
            Message = f.Message,
            SubmittedOn = f.CreatedAt is null
                ? null : DateOnly.FromDateTime(f.CreatedAt.Value),
            FromRegisteredUser = f.UserId is not null
        }).ToList();
    }

    public async Task<long> CountNewAsync() =>
        await _db.Feedbacks.CountAsync(f => f.Status == FeedbackCategory.New);

    private static string ShortenName(string name)
    {
        if (string.IsNullOrWhiteSpace(name)) return "A visitor";

        var parts = name.Trim().Split(' ', StringSplitOptions.RemoveEmptyEntries);
        if (parts.Length == 1) return parts[0];

        var last = parts[^1];
        return $"{parts[0]} {last[0]}.";
    }

}
