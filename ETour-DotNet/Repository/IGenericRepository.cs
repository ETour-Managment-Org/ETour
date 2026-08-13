using System.Linq.Expressions;

namespace ETour.Api.Repository;

public interface IGenericRepository<T> where T : class
{
    Task<IReadOnlyList<T>> GetAllAsync(CancellationToken ct = default);

    Task<IReadOnlyList<T>> FindAsync(Expression<Func<T, bool>> predicate,
                                     CancellationToken ct = default);

    Task<T> GetByIdAsync(object id, CancellationToken ct = default);

    Task<T> SingleOrDefaultAsync(Expression<Func<T, bool>> predicate,
                                 CancellationToken ct = default);

    Task<bool> ExistsAsync(Expression<Func<T, bool>> predicate, CancellationToken ct = default);

    Task<int> CountAsync(Expression<Func<T, bool>> predicate = null,
                         CancellationToken ct = default);

    Task<T> AddAsync(T entity, CancellationToken ct = default);

    Task AddRangeAsync(IEnumerable<T> entities, CancellationToken ct = default);

    Task<T> UpdateAsync(T entity, CancellationToken ct = default);

    Task RemoveAsync(T entity, CancellationToken ct = default);

    Task<int> SaveChangesAsync(CancellationToken ct = default);

    IQueryable<T> Query(bool tracked = false);
}
