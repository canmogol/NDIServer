import com.fererlab.action.BaseAction;
import com.fererlab.action.ModelAction;
import com.fererlab.dto.Request;
import com.fererlab.dto.Response;
import com.fererlab.ndi.Wire;
import com.ndi.app.model.Department;
import com.ndi.app.service.LDAPService;

import java.util.List;

public class UserAction extends BaseAction {

    @Wire
    private LDAPService ldapService;

    Response doSomething(Request request) {
        ModelAction<Department> modelAction = new ModelAction<Department>(Department.class);
        List<Department> list = modelAction.findAll();
        return Ok(request).add("data", list).add("test", "99").toResponse();
    }

}