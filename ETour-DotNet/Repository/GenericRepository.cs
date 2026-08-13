using System.Linq.Expressions;
using ETour.Api.Data;
using Microsoft.EntityFrameworkCore;

namespace ETour.Api.Repository;

public class GenericRepository<T> : IGenericRepository<T> where T : class
{
    protected readonly ETourDbContext Db;
    protected readonly DbSet<T> Set;
    private readonly ILogger<GenericRepository<T>> _log;

    public GenericRepository(ETourDbContext db, ILogger<GenericRepository<T>> log)
    {
        Db = db;
        Set = db.Set<T>();
        _log = log;
    }

    public async Task<IReadOnlyList<T>> GetAllAsync(CancellationToken ct = default) =>
        await Set.AsNoTracking().ToListAsync(ct);

    public async Task<IReadOnlyList<T>> FindAsync(Expression<Func<T, bool>> predicate,
                                                  CancellationToken ct = default) =>
        await Set.AsNoTracking().Where(predicate).ToListAsync(ct);

    public async Task<T> GetByIdAsync(object id, CancellationToken ct = default) =>
        await Set.FindAsync(new[] { id }, ct);

    public async Task<T> SingleOrDefaultAsync(Expression<Func<T, bool>> predicate,
                                              CancellationToken ct = default) =>
        await Set.AsNoTracking().FirstOrDefaultAsync(predicate, ct);

    public async Task<bool> ExistsAsync(Expression<Func<T, bool>> predicate,
                                        CancellationToken ct = default) =>
        await Set.AnyAsync(predicate, ct);

    public async Task<int> CountAsync(Expression<Func<T, bool>> predicate = null,
                                      CancellationToken ct = default) =>
        predicate is null
            ? await Set.CountAsync(ct)
            : await Set.CountAsync(predicate, ct);

    public async Task<T> AddAsync(T entity, CancellationToken ct = default)
    {
        await Set.AddAsync(entity, ct);
        await Db.SaveChangesAsync(ct);
        _log.LogDebug("Added {Entity}", typeof(T).Name);
        return entity;
    }

    public async Task AddRangeAsync(IEnumerable<T> entities, CancellationToken ct = default)
    {
        await Set.AddRangeAsync(entities, ct);
        await Db.SaveChangesAsync(ct);
    }

    public async Task<T> UpdateAsync(T entity, CancellationToken ct = default)
    {
        Set.Update(entity);
        await Db.SaveChangesAsync(ct);
        _log.LogDebug("Updated {Entity}", typeof(T).Name);
        return entity;
    }

    public async Task RemoveAsync(T entity, CancellationToken ct = default)
    {
        Set.Remove(entity);
        await Db.SaveChangesAsync(ct);
        _log.LogDebug("Removed {Entity}", typeof(T).Name);
    }

    public async Task<int> SaveChangesAsync(CancellationToken ct = default) =>
        await Db.SaveChangesAsync(ct);

    public IQueryable<T> Query(bool tracked = false) =>
        tracked ? Set : Set.AsNoTracking();
}
