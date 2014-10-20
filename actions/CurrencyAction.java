import com.fererlab.action.BaseAction;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;

public class CurrencyAction extends BaseAction {

    Response list(Request request) {
        getXStreamJSON().alias("currencyList", net.webservicex.Currency.class);
        return Ok(request).add("data", net.webservicex.Currency.values()).toResponse();
    }

}