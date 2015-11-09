import com.fererlab.action.ModelAction;
import com.fererlab.session.SessionUser;
import com.ndi.app.model.Department;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * acm
 */
public class MapConversionTest {

    private XStream xStreamJSON = new XStream(new JettisonMappedXmlDriver());

    public static void main(String[] args) {
        new MapConversionTest();
    }

    public MapConversionTest() {
        xStreamJSON.setMode(XStream.SINGLE_NODE_XPATH_RELATIVE_REFERENCES);
//        xStreamJSON.autodetectAnnotations(true);
//        runTests();
    }

    private void runTests() {
        Map<String, Object> entityMap = new HashMap<>();
        ModelAction<Department> modelAction = new ModelAction<>(Department.class);
        List<Department> departments = modelAction.findAll();
        entityMap.put("departments", departments);
        SessionUser sessionUser = new SessionUser();
        sessionUser.setUsername("acm");
        sessionUser.setSessionId("b226ad60e2ac3a18c4641721da4e6338ba26837d");
        sessionUser.getGroups().add("admin");
        sessionUser.getGroups().add("*");
        sessionUser.getGroups().add("user");
        sessionUser.getProperties().put("departments", departments);
        String map = xStreamJSON.toXML(entityMap);
        String user = xStreamJSON.toXML(sessionUser);

        Object objectMap = xStreamJSON.fromXML(map);
        SessionUser sessionUserFrom = (SessionUser) xStreamJSON.fromXML(user);
        System.out.println("");
    }
}
