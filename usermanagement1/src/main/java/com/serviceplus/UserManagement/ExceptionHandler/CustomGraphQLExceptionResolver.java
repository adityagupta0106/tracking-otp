package com.serviceplus.UserManagement.ExceptionHandler;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CustomGraphQLExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof SPRuntimeError spEx) {

            Map<String, Object> extensions = new HashMap<>();
            extensions.put("code", spEx.getErrorCode().value());
            extensions.put("httpStatus", spEx.getErrorCode().toString());

            return GraphqlErrorBuilder.newError(env)
                    .message(spEx.getMessage())
                    .extensions(extensions)
                    .build();
        }

        return null;
    }
}

