package org.jboss.arquillian.example.openshift.cluster.jboss;

import java.io.IOException;

import javax.annotation.Resource;

import org.infinispan.Cache;
import org.infinispan.manager.CacheContainer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.TargetsContainer;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:mlazar@redhat.com">Matej Lazar</a>
 */
@RunWith(Arquillian.class)
public class CacheReplicationTestCase {

    //add eager start to cache config <cache-container name="cluster" aliases="ha-partition" default-cache="default" start="EAGER">
    @Resource(mappedName="java:jboss/infinispan/container/cluster")
    CacheContainer container;

    private Cache<String, String> cache;

    private Cache<String, String> getCache() {
        if (cache == null) {
            cache = container.getCache();
        }
        return cache;
    }


    /**
     * Wake-up cache on first node.
     */
    @InSequence(10)
    @Test
    @OperateOnDeployment("dep1")
    public void wakeUpCacheA() throws IOException {
        getCache();
    }

    /**
     * Wake-up cache on second node.
     */
    @InSequence(20)
    @Test
    @OperateOnDeployment("dep2")
    public void wakeUpCacheB() throws IOException {
        waitForSync();
        getCache();
    }


    @InSequence(100)
    @Test
    @OperateOnDeployment("dep1")
    public void testCacheOnDepA() {
        waitForSync();
        System.out.println(">>> Running test on dep1: writting to cache.");
        getCache().put("key", "value");
    }

    @InSequence(110)
    @Test
    @OperateOnDeployment("dep1")
    public void readFromCacheOnDepA() {
        waitForSync();
        System.out.println(">>> Running test on dep1: reading from cache.");
        String value = getCache().get("key");
        System.out.println(">>> Cache on dep1:" + value);
        Assert.assertEquals("value", value);
    }

    @InSequence(120)
    @Test
    @OperateOnDeployment("dep2")
    public void readFromCacheOnDepB() {
        waitForSync();
        System.out.println(">>> Running test on dep2: reading from cache.");
        String value = getCache().get("key");
        System.out.println(">>> Cache on dep2:" + value);
        Assert.assertEquals("value", value);
    }

    @InSequence(1000)
    @Test
    @OperateOnDeployment("dep1")
    public void cleanUp() throws IOException {
        waitForSync();
        getCache().clear();
    }

    @Deployment (name = "dep1") @TargetsContainer("openshift-node1")
    public static WebArchive getDeploymentA() {
        return getDeployment();
    }

    @Deployment(name = "dep2") @TargetsContainer("openshift-node2")
    public static WebArchive getDeploymentB() {
        return getDeployment();
    }

    public static WebArchive getDeployment() {
        return ShrinkWrap.create(WebArchive.class, "cache-repl.war")
            .setWebXML("web.xml")
            .addAsManifestResource("jboss-deployment-structure.xml");
    }

    private void waitForSync() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


}
