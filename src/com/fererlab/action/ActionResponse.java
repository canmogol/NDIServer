package com.fererlab.action;

import com.fererlab.dto.*;

/**
 * acm
 */
public abstract class ActionResponse {

    public class PrepareResponse {
        private Request request;
        private Status status;
        private ServerResponse serverResponse;

        public PrepareResponse(Request request, Status status, ServerResponse serverResponse) {
            this.request = request;
            this.status = status;
            this.serverResponse = serverResponse;
        }

        public PrepareResponse header(String key, Object value) {
            serverResponse.header(key, value);
            return this;
        }

        public PrepareResponse add(String key, Object value) {
            serverResponse.add(key, value);
            return this;
        }

        public PrepareResponse exception(Exception e) {
            serverResponse.exception("class", e.getClass().getName());
            serverResponse.exception("message", e.getMessage());
            String causeNames = "";
            String causeMessages = "";
            if (e.getCause() != null) {
                Throwable cause = e.getCause();
                while (true) {
                    cause = cause.getCause();
                    if (cause == null) {
                        break;
                    }
                    causeNames += cause.getClass().getName() + ", ";
                    causeMessages += cause.getMessage() + ", ";
                }
            }
            serverResponse.exception("causeNames", causeNames);
            serverResponse.exception("causeMessages", causeMessages);
            return this;
        }

        public Response toResponse() {
            return Response.create(request, toContent(request, serverResponse), status);
        }
    }

    public abstract String toContent(Request request, Object... objects);

    private PrepareResponse action(Request request, String message, StatusCode statusCode, Status st) {
        return new PrepareResponse(request, st, new ServerResponse(statusCode.getName(), statusCode.getCode(), message));
    }

    public PrepareResponse Ok(Request request) {
        return Ok(request, "");
    }

    public PrepareResponse Ok(Request request, String message) {
        return action(request, message, StatusCode.SUCCESS, Status.STATUS_OK);
    }

    public PrepareResponse Created(Request request) {
        return Created(request, "");
    }

    public PrepareResponse Created(Request request, String message) {
        return action(request, message, StatusCode.SUCCESS, Status.STATUS_CREATED);
    }

    public PrepareResponse BadRequest(Request request, String message) {
        return action(request, message, StatusCode.FAIL, Status.STATUS_BAD_REQUEST);
    }

    public PrepareResponse Unauthorized(Request request, String message) {
        return action(request, message, StatusCode.FAIL, Status.STATUS_UNAUTHORIZED);
    }

    public PrepareResponse NoContent(Request request, String message) {
        return action(request, message, StatusCode.FAIL, Status.STATUS_NO_CONTENT);
    }

    public PrepareResponse NotFound(Request request, String message) {
        return action(request, message, StatusCode.FAIL, Status.STATUS_NOT_FOUND);
    }

    public PrepareResponse NotModified(Request request) {
        return NotModified(request, "");
    }

    public PrepareResponse NotModified(Request request, String message) {
        return action(request, message, StatusCode.SUCCESS, Status.STATUS_NOT_MODIFIED);
    }

    public PrepareResponse Redirect(Request request) {
        return Redirect(request, "");
    }

    public PrepareResponse Redirect(Request request, String message) {
        return action(request, message, StatusCode.SUCCESS, Status.STATUS_TEMPORARY_REDIRECT);
    }

    public PrepareResponse RedirectToStatic(Request request) {
        return RedirectToStatic(request, "");
    }

    public PrepareResponse RedirectToStatic(Request request, String message) {
        return action(request, message, StatusCode.SUCCESS, Status.STATUS_PERMANENT_REDIRECT);
    }

    public PrepareResponse Error(Request request, String message) {
        return action(request, message, StatusCode.FAIL, Status.STATUS_INTERNAL_SERVER_ERROR);
    }

    public PrepareResponse Forbidden(Request request, String message) {
        return action(request, message, StatusCode.FAIL, Status.STATUS_FORBIDDEN);
    }

}
