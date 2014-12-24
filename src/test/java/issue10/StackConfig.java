package issue10;

import com.github.tonybaines.gestalt.Default;

public interface StackConfig {

    @Default.String("100")
    String getNodeName();

    @Default.String("100:0.0.0.0:8899")
    String getClusterNodes();

    @Default.String("bt")
    String getEnterpriseName();

    @Default.String("Cluster1")
    String getClusterName();

}
