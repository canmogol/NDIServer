import com.fererlab.action.BaseAction;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;
import com.fererlab.ndi.Wire;
import com.ndi.app.service.WSService;

import java.time.DateTimeException;

public class CurrencyAction extends BaseAction {

    @Wire
    private WSService wsService;

    public Response convert(Request r) {
        try {
            Object argObject = getXStreamJSON().fromXML(r.get("args").replace("%22", "\""));
            Object wsResponse = wsService.getWSResponse(
                    r.get("service"),
                    r.get("method"),
                    argObject
            );
            return Ok(r).add("data", wsResponse).toResponse();
        } catch (Exception e) {
            return Error(r, e.getMessage()).exception(e).toResponse();
        }
    }

    public Response test(Request request) {
        return Error(request, "got exception :(").exception(new DateTimeException("No time for this!")).toResponse();
    }

    public Response list(Request request) {
        getXStreamJSON().alias("currencyList", net.webservicex.Currency.class);
        return Ok(request)
                .add("data", net.webservicex.Currency.values())
                .toResponse();
    }

}