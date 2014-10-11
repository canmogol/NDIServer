import com.fererlab.action.BaseAction
import com.fererlab.action.ModelAction
import com.fererlab.db.EM
import com.fererlab.dto.Param
import com.fererlab.dto.ParamMap
import com.fererlab.dto.Request
import com.fererlab.dto.Response
import com.ndi.app.model.Department
import com.thoughtworks.xstream.annotations.XStreamImplicit

import javax.persistence.EntityManager
import javax.persistence.Query
import javax.persistence.TypedQuery
import javax.persistence.criteria.*

import static com.fererlab.dto.ParamRelation.EQ
import static com.fererlab.dto.ParamRelation.GE

public class DepartmentAction extends BaseAction {

    Response listModelAll(Request request) {
        ModelAction<Department> modelAction = new ModelAction<Department>(Department.class);
        List<Department> list = modelAction.findAll();
        return Ok(request).add("data", list).toResponse();
    }

    Response listModelKeyValue(Request request) {
        ModelAction<Department> modelAction = new ModelAction<Department>(Department.class);
        List<Department> list = modelAction.findAll("name", "Sports");
        return Ok(request).add("data", list).toResponse();
    }

    Response listModelQuery(Request request) {
        List<Department> list = query(Department.class).add("name", EQ, "Sports").findAll();
        return Ok(request).add("data", list).toResponse();
    }

    Response listModelParam(Request request) {
        ModelAction<Department> modelAction = new ModelAction<Department>(Department.class);
        ParamMap<String, Param<String, Object>> searchCriteria = new ParamMap<String, Param<String, Object>>();
        searchCriteria.addParam(new Param<String, Object>("name", "Sports", EQ));
        List<Department> list = modelAction.findAll(searchCriteria);
        return Ok(request).add("data", list).toResponse();
    }

    Response listNativeQuery(Request request) {
        EntityManager entityManager = EM.getEntityManager();
        Query nativeQuery = entityManager.createNativeQuery("select d.* from department as d where d.name='Sports'");
        List list = nativeQuery.getResultList();
        return Ok(request).add("data", list).toResponse();
    }

    Response listNamedQuery(Request request) {
        EntityManager entityManager = EM.getEntityManager();
        Query nativeQuery = entityManager.createNamedQuery(Department.FIND_NAME);
        nativeQuery.setParameter("name", "Sports");
        List list = nativeQuery.getResultList();
        return Ok(request).add("data", list).toResponse();
    }

    Response listQuery(Request request) {
        EntityManager entityManager = EM.getEntityManager();
        Query nativeQuery = entityManager.createQuery("select d from Department d where d.name=:name");
        nativeQuery.setParameter("name", "Sports");
        List list = nativeQuery.getResultList();
        return Ok(request).add("data", list).toResponse();
    }

    Response listCriteriaQuery(Request request) {
        EntityManager entityManager = EM.getEntityManager();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Department> criteriaQuery = cb.createQuery(Department.class);
        Root<Department> departmentRoot = criteriaQuery.from(Department.class);
        Expression expression = departmentRoot.get("name");
        Predicate predicate = expression.in("Sports");
        criteriaQuery.where(predicate);
        TypedQuery<Department> q = entityManager.createQuery(criteriaQuery);
        List list = q.getResultList();
        return Ok(request).add("data", list).toResponse();
    }

}