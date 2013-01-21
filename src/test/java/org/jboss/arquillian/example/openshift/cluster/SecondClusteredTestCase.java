package org.jboss.arquillian.example.openshift.cluster;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:mlazar@redhat.com">Matej Lazar</a>
 */
@RunWith(Arquillian.class)
public class SecondClusteredTestCase {

    @Deployment (name = "dep1") @TargetsContainer("openshift-node1")
    public static WebArchive getDeploymentA() {
        return getDeployment();
    }

    @Deployment(name = "dep2") @TargetsContainer("openshift-node2")
    public static WebArchive getDeploymentB() {
        return getDeployment();
    }

    public static WebArchive getDeployment() {
        return ShrinkWrap.create(WebArchive.class, "cluster-tests2.war")
            .setWebXML("web.xml")
            .addAsManifestResource("jboss-deployment-structure.xml");
    }

    @InSequence(10)
    @Test
    @OperateOnDeployment("dep1")
    public void testCacheOnDepA() {
        System.out.println("Running test " + this.getClass().getName() + " on dep1.");
    }

    @InSequence(20)
    @Test
    @OperateOnDeployment("dep2")
    public void testCacheOnDepB() {
        System.out.println("Running test " + this.getClass().getName() + " on dep2.");
    }

    @InSequence(30)
    @Test
    @OperateOnDeployment("dep1")
    public void testCacheOnDepA1() {
        System.out.println("Running 2nd test " + this.getClass().getName() + " on dep1.");
    }

    @InSequence(40)
    @Test
    @OperateOnDeployment("dep2")
    public void testCacheOnDepB1() {
        System.out.println("Running 2nd test " + this.getClass().getName() + " on dep2..");
    }

    @InSequence(50)
    @Test
    @OperateOnDeployment("dep1")
    public void undeploy() {
        //dummy test to separate execution time from undeploy
    }
}
