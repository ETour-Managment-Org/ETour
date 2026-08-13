namespace ETour.Api.Exceptions;

public class ResourceNotFoundException : Exception
{
    public ResourceNotFoundException(string message) : base(message) { }

    public ResourceNotFoundException(string entity, object id)
        : base($"{entity} not found with id {id}") { }
}

public class BadCredentialsException : Exception
{
    public BadCredentialsException(string message) : base(message) { }
}

public class AccessDeniedException : Exception
{
    public AccessDeniedException(string message) : base(message) { }
}

public class ConflictException : Exception
{
    public ConflictException(string message) : base(message) { }
}

public class ApiError
{
    public DateTime Timestamp { get; set; }
    public int Status { get; set; }
    public string Error { get; set; }
    public string Message { get; set; }
    public string Path { get; set; }
}
