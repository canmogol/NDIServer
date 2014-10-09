import com.fererlab.action.BaseAction;
import com.fererlab.action.ModelAction;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;
import com.fererlab.ndi.Wire;
import com.ndi.app.model.Department;
import com.ndi.app.service.LDAPService;

import java.util.List;

public class CurrencyAction extends BaseAction {

    Response list(Request request) {
        getXStreamJSON().alias("currencyList", net.webservicex.Currency.class);
        return Ok(request).add("data", net.webservicex.Currency.values()).toResponse();
    }

}