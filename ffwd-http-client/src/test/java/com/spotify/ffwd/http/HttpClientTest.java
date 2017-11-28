package com.spotify.ffwd.http;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IPing;
import com.netflix.loadbalancer.LoadBalancerBuilder;
import com.netflix.loadbalancer.Server;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HttpClientTest {

    private Batch.Point point;

    private Batch batch;

    private HttpClient client;

    @Mock
    private HttpDiscovery discovery;

    @Mock
    private RawHttpClientFactory rawHttpClientFactory;

    @Mock
    private RawHttpClient rawHttpClient;

    @Mock
    private LoadBalancerBuilder<Server> loadBalancerBuilder;

    @Mock
    private ILoadBalancer loadBalancer;

    @Mock
    private Server server;

    private String searchDomain = "test_search_domain";

    @Before
    public void setup() {
        point = new Batch.Point("test_key", ImmutableMap.of("what", "error-rate"), 1234L, 11111L);

        batch = new Batch(ImmutableMap.of("what", "error-rate"), ImmutableList.of(point));

    }

    @Test
    public void testSendBatch() {

        when(rawHttpClientFactory.newClient(any(Server.class))).thenReturn(rawHttpClient);

        when(discovery.apply(any(), any())).thenReturn(loadBalancerBuilder);

        when(loadBalancerBuilder.withPing(any(IPing.class))).thenReturn(loadBalancerBuilder);
        when(loadBalancer.chooseServer(any())).thenReturn(server);
        //when(loadBalancer.chooseServer()).thenReturn(server);
//        when(zoneAwareLoadBalancer.getServerListImpl()).thenReturn(ServerList.class);

        //when(loadBalancerBuilder.buildDynamicServerListLoadBalancer()).thenReturn(loadBalancer);

        when(rawHttpClient.sendBatch(any(Batch.class))).thenReturn(null);

        client = new HttpClient(rawHttpClientFactory, loadBalancer);

        ArgumentCaptor<Batch> captor = ArgumentCaptor.forClass(Batch.class);

        client.sendBatch(batch);
        verify(loadBalancer, times(1)).chooseServer(any());
        //verify(rawHttpClient, times(1)).sendBatch(captor.capture());

        //Batch sentBatch = captor.getValue();
        //assertEquals("error-rate", sentBatch.getCommonTags().get("what"));
        //assertEquals(point, sentBatch.getPoints().get(0));
    }
}
