import { Document } from "mongodb";
import { UpdateMovieRequest } from "../types";

const ALLOWED_FILTER_FIELDS = new Set([
  "_id",
  "title",
  "year",
  "plot",
  "fullplot",
  "genres",
  "directors",
  "writers",
  "cast",
  "countries",
  "languages",
  "rated",
  "runtime",
  "poster",
]);

const ALLOWED_OPERATORS = new Set([
  "$in",
  "$nin",
  "$gt",
  "$gte",
  "$lt",
  "$lte",
  "$ne",
  "$exists",
]);

const UPDATE_FIELDS: (keyof UpdateMovieRequest)[] = [
  "title",
  "year",
  "plot",
  "fullplot",
  "genres",
  "directors",
  "writers",
  "cast",
  "countries",
  "languages",
  "rated",
  "runtime",
  "poster",
];

export function escapeRegexLiteral(value: string): string {
  return value.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");
}

export class InvalidMongoQueryError extends Error {
  constructor(message: string) {
    super(message);
    this.name = "InvalidMongoQueryError";
  }
}

function sanitizeOperatorValue(value: unknown): unknown {
  if (value === null || typeof value !== "object" || Array.isArray(value)) {
    return value;
  }

  const operatorMap = value as Record<string, unknown>;
  const sanitized: Record<string, unknown> = {};

  for (const [operator, operatorValue] of Object.entries(operatorMap)) {
    if (!operator.startsWith("$")) {
      throw new InvalidMongoQueryError(
        `Unsupported filter operator key '${operator}'`
      );
    }
    if (!ALLOWED_OPERATORS.has(operator)) {
      throw new InvalidMongoQueryError(
        `Unsupported MongoDB operator '${operator}'`
      );
    }
    sanitized[operator] = operatorValue;
  }

  return sanitized;
}

export function sanitizeBatchFilter(filter: Record<string, unknown>): Document {
  if (!filter || typeof filter !== "object" || Array.isArray(filter)) {
    throw new InvalidMongoQueryError("Filter must be a non-array object");
  }

  const sanitized: Document = {};

  for (const [key, value] of Object.entries(filter)) {
    if (key.startsWith("$")) {
      throw new InvalidMongoQueryError(
        `Top-level operator '${key}' is not allowed`
      );
    }
    if (!ALLOWED_FILTER_FIELDS.has(key)) {
      throw new InvalidMongoQueryError(`Filter field '${key}' is not allowed`);
    }

    if (value !== null && typeof value === "object" && !Array.isArray(value)) {
      sanitized[key] = sanitizeOperatorValue(value);
    } else {
      sanitized[key] = value;
    }
  }

  return sanitized;
}

export function sanitizeUpdateFields(
  update: Record<string, unknown>
): UpdateMovieRequest {
  const sanitized: UpdateMovieRequest = {};

  for (const key of Object.keys(update)) {
    if (key.startsWith("$")) {
      throw new InvalidMongoQueryError(
        `Update operator '${key}' is not allowed`
      );
    }
    if (!UPDATE_FIELDS.includes(key as keyof UpdateMovieRequest)) {
      throw new InvalidMongoQueryError(`Update field '${key}' is not allowed`);
    }
    sanitized[key as keyof UpdateMovieRequest] = update[key] as never;
  }

  return sanitized;
}
