using ETour.Api.Models;

namespace ETour.Api.Common;

public static class CategoryScope
{
    private const int MaxDepth = 20;

    private static readonly Dictionary<string, HashSet<string>> Themes = new()
    {
        ["SPT"] = new HashSet<string> { "CKD", "ECD", "SLC" },
        ["CKD"] = new HashSet<string> { "ECD", "SLC" },
        ["ECD"] = new HashSet<string> { "CKD", "SLC" },
        ["SLC"] = new HashSet<string> { "CKD", "ECD" }
    };

    public static HashSet<int> Resolve(int? categoryId, IEnumerable<Category> allCategories)
    {
        var scope = new HashSet<int>();
        if (categoryId is null)
        {
            return scope;
        }

        var childrenOf = new Dictionary<int, List<int>>();
        var codeOf = new Dictionary<int, string>();
        var idsByCode = new Dictionary<string, List<int>>();

        foreach (var c in allCategories)
        {
            codeOf[c.CategoryId] = c.CatCode;

            if (c.CatCode is not null)
            {
                if (!idsByCode.TryGetValue(c.CatCode, out var byCode))
                {
                    byCode = new List<int>();
                    idsByCode[c.CatCode] = byCode;
                }
                byCode.Add(c.CategoryId);
            }

            if (c.ParentId is int parentId)
            {
                if (!childrenOf.TryGetValue(parentId, out var kids))
                {
                    kids = new List<int>();
                    childrenOf[parentId] = kids;
                }
                kids.Add(c.CategoryId);
            }
        }

        AddWithDescendants(categoryId.Value, childrenOf, scope);

        if (codeOf.TryGetValue(categoryId.Value, out var code) && code is not null
            && Themes.TryGetValue(code, out var members))
        {
            foreach (var member in members)
            {
                if (!idsByCode.TryGetValue(member, out var ids)) continue;
                foreach (var id in ids)
                {
                    AddWithDescendants(id, childrenOf, scope);
                }
            }
        }

        return scope;
    }

    private static void AddWithDescendants(int rootId,
                                           Dictionary<int, List<int>> childrenOf,
                                           HashSet<int> into)
    {
        var queue = new Queue<int>();
        queue.Enqueue(rootId);

        var guard = 0;
        while (queue.Count > 0 && guard++ < MaxDepth * 100)
        {
            var current = queue.Dequeue();
            if (!into.Add(current))
            {
                continue;
            }
            if (childrenOf.TryGetValue(current, out var kids))
            {
                foreach (var k in kids) queue.Enqueue(k);
            }
        }
    }
}
