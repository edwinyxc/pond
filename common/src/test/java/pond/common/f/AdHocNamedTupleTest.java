package pond.common.f;

import org.junit.Test;
import pond.common.S;

import static org.junit.Assert.assertTrue;

public class AdHocNamedTupleTest {
    @Test
    public void just_a_demo_for_creating_ad_hoc_tuple() {
        String _name = "ed";
        String _fullName = "edwin_yxc";
        var adhocNamedTuple = new Tuple<>(_name, _fullName){
            String name = _name;
            String fullName = _fullName;
        };
        adhocNamedTuple.tap((name, fullName) -> {
            assertTrue("",
                S._same(adhocNamedTuple.name, adhocNamedTuple._a, name, _name)
            );
            assertTrue("",
                S._same(adhocNamedTuple.fullName, adhocNamedTuple._b, fullName, _fullName)
            );
        });
    }
}
