package com.phenxie.k8s.common;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
public class K8SUtils {

    public static final String APIVersion="v1";
    public static final String KIND_NAMESPACE="Namespace";
    public static final String KIND_SERVICE="Service";
    public static final String KIND_POD="Pod";
    public static final String SPEC_Type_NodePort="NodePort";
    public static final String Spec_ClusterIP_None="None";

    /***
     * 根据配置文件还构建api对象
     * @return
     */
    static CoreV1Api getAPI(){
        try {
            ApiClient client;
            //通过配置文件来构建apiclient
            String kubeConfigPath = "/Users/phenxie/103/192.168.8.240.config"; //8.240
//            kubeConfigPath="/Users/phenxie/103/18.11/config";
            client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath))).build();
            Configuration.setDefaultApiClient(client);
            CoreV1Api api = new CoreV1Api();
            return api;
        }catch (Exception ex){

        }
        return null;
    }

    /***
     * 遍历所有的namespace 查看指定的namespace是否存在
     * @param namespace
     * @return
     */
    public static boolean isNamespaceExists(String namespace){
        CoreV1Api api=getAPI();
        try{
            V1NamespaceList list=  api.listNamespace(null,null,null,null,null,null
                    ,null,null,null,null);
            AtomicBoolean isExists= new AtomicBoolean(false);
            list.getItems().forEach(n->{
                if(n.getMetadata().getName().toLowerCase().equals(namespace.toLowerCase())){
                    isExists.set(true);
                }
            });
            return isExists.get();
        }catch (ApiException ex){

            return false;
        }
    }

    /***
     * 删除namespace isDeleteSub标识是否删除namespace下的service和pod
     * @param namespace
     * @param isDeleteSub
     * @return
     */
    public static ApiException deleteNamespace(String namespace,boolean isDeleteSub){
        CoreV1Api api=getAPI();
        //如果namespace不存在，返回true
        if(!isNamespaceExists(namespace)){
            return  null;
        }
        //是否删除namespace下的service和pod
        if (isDeleteSub){
            try{
                //删除service
                api.listNamespacedService(namespace,null,null,null,null,null,null,null,null,null,null)
                        .getItems().forEach(s->{
                    try {
                        api.deleteNamespacedService(s.getMetadata().getName(), namespace.toLowerCase(), null, null, null, null, null, null);
                    }catch (ApiException ex){

                    }
                });
                //删除pod
                api.listNamespacedPod(namespace,null,null,null,null,null,null,null,null,null,null)
                        .getItems().forEach(p->{
                    try{
                        api.deleteNamespacedPod(p.getMetadata().getName(),namespace.toLowerCase(),null,null,null,null,null,null);
                    }
                    catch (ApiException ex){

                    }
                });

            }catch (ApiException ex){
                return ex;
            }
        }
        try {
            //检查service和pod是否完全删除
            if(api.listNamespacedPod(namespace.toLowerCase(),null,null,null,null,null,null,null,null,null,null).getItems().size()==0
                    &&api.listNamespacedService(namespace.toLowerCase(),null,null,null,null,null,null,null,null,null,null).getItems().size()==0){
                //删除namespace
                api.deleteNamespace(namespace.toLowerCase(),null,null,null,null,null,null);
            }else{
                return null;
            }
        }catch (ApiException ex){
            return  ex;
        }
        return null;
    }

    /***
     * 删除namespace下的Service
     * @param namespace
     * @param serviceName
     * @return
     */
    public static  ApiException deleteNameSpaceService(String namespace,String serviceName){
        try{
            getAPI().deleteNamespacedService(serviceName.toLowerCase(),namespace.toLowerCase(),null,null,null,null,null, null);
            return  null;
        }catch (ApiException ex){
            return ex;
        }
    }

    /***
     * 删除namespace下的pod
     * @param namespace
     * @param podName
     * @return
     */
    public static  ApiException deleteNameSpacePod(String namespace,String podName){
        try{
            getAPI().deleteNamespacedPod(podName.toLowerCase(),namespace.toLowerCase(),null,null,null,null,null, null);
            return  null;
        }catch (ApiException ex){
            return ex;
        }
    }

    /****
     * 创建虚拟机 返回为null表示没有异常，创建成功！
     * @param nameSpace namespace要求全局唯一 会被转换成小写
     * @param imageURL 镜像地址
     * @param hostname 虚拟机的主机名 要求在namespace中是唯一的 作为其他的在同一个namespace中的主机访问的标识 会被转换成小写
     * @param portsMap 开放的端口映射
     * @param isExpose 是否开放给外部访问
     * @param islimit 是否限制内存和cpu，false表示不限制，设置false之后 cpucount和memsize将不起作用 true表示限制内存和cpu
     * @param cpuCount cpu的数量 小于1或者大于10 被设置成1
     * @param memSizeG 内存的大小 单位为G 小于1或者大于10 被设置成1
     * @return
     */
    public static ApiException createEndPoint(String nameSpace, String imageURL, String hostname, Map<String,Integer> portsMap,boolean isExpose,boolean islimit,int cpuCount,int memSizeG){

        CoreV1Api api=getAPI();
        nameSpace=nameSpace.toLowerCase();
        hostname=hostname.toLowerCase();

        //构建namespace对象
        V1Namespace namespace=new V1Namespace();
        namespace.setApiVersion(APIVersion);
        namespace.setKind(KIND_NAMESPACE);
        V1ObjectMeta meta=new V1ObjectMeta();
        meta.setName(nameSpace.toLowerCase());
        HashMap<String,String> map=new HashMap<>();
        //指定namespace的name 这个name需要是全局唯一的
        map.put("name",nameSpace.toLowerCase());
        meta.setLabels(map);
        namespace.setMetadata(meta);

        try{
            //如果namespace 不存在 就创建namespace
            if(!isNamespaceExists(nameSpace)) {
                api.createNamespace(namespace, null, null, null);
            }
        }catch (ApiException e){
//            System.out.println("创建 namespace 失败");
//            System.out.println("Status code: {}"+e.getCode());
//            System.out.println("Reason: {}"+ e.getResponseBody());
//            System.out.println("Response headers: {}"+ e.getResponseHeaders());
            return e;
        }

        //创建serivce
        V1Service service=new V1Service();

        service.setApiVersion(APIVersion);
        service.setKind(KIND_SERVICE);
        V1ObjectMeta serviceMeta=new V1ObjectMeta();
        serviceMeta.setName(hostname);
        service.setMetadata(serviceMeta);
        V1ServiceSpec serviceSpec=new V1ServiceSpec();
        HashMap<String,String> serviceSpecMap=new HashMap<>();
        serviceSpecMap.put("app",hostname);
        serviceSpec.setSelector(serviceSpecMap);
        List<V1ServicePort> ports=new ArrayList<>();

        //nodeport 是外部访问的链接 The range of valid ports is 30000-32767
        // 设置了targetPort 会自动分配端口
        // nodePort 使用指定的外部端口
        //isExpose 表示这个节点是否能够被外部访问
        if(isExpose) {
            serviceSpec.setType("NodePort");
            for (String  port :portsMap.keySet()) {
                ports.add(new V1ServicePort().name(port).port(portsMap.get(port)).targetPort(new IntOrString(portsMap.get(port))));
            }
        }else{
            serviceSpec.setClusterIP("None");
            for (String  port :portsMap.keySet()) {
                ports.add(new V1ServicePort().name(port).port(portsMap.get(port)));
            }
        }
        serviceSpec.setPorts(ports);
        service.setSpec(serviceSpec);

        try {
            api.createNamespacedService(nameSpace, service, null, null, null);
        }catch (ApiException e){
//            System.out.println("创建Service失败");
//            System.out.println("Status code: {}"+e.getCode());
//            System.out.println("Reason: {}"+ e.getResponseBody());
//            System.out.println("Response headers: {}"+ e.getResponseHeaders());
            return e;
        }


        //master 节点
        V1Pod masterPod=new V1Pod();
        masterPod.setApiVersion(APIVersion);
        masterPod.setKind(KIND_POD);
        V1ObjectMeta masterPodMeta=new V1ObjectMeta();
        masterPodMeta.setName(hostname);

        HashMap<String,String> masterPodMap=new HashMap<>();
        masterPodMap.put("app",hostname);
        masterPodMeta.setLabels(masterPodMap);
        masterPod.setMetadata(masterPodMeta);

        V1PodSpec v1PodSpec=new V1PodSpec();
        List<V1Container> masterContainer=new ArrayList<>();
        V1Container master=new V1Container();
        master.setImage(imageURL);
        master.setName(hostname);

        master.setImagePullPolicy("IfNotPresent");
        List<V1ContainerPort> v1ContainerPorts=new ArrayList<>();
        for (String  port :portsMap.keySet()) {
            v1ContainerPorts.add(new V1ContainerPort().containerPort(portsMap.get(port)));
        }
        master.setPorts(v1ContainerPorts);
        V1ResourceRequirements resourceRequirements=new V1ResourceRequirements();
        //设置pod的核心和内存大小
//        resourceRequirements.putRequestsItem("cpu", Quantity.fromString("1")).putRequestsItem("memory",Quantity.fromString("1G"));
        //设置pod的限制的核心和内存大小 如果cpucount 小于1或者大于10 则cpucount为1
        // memSizeG  小于1或者大于10 则memSizeG为1
        if(cpuCount<=1||cpuCount>=10){
            cpuCount=1;
        }
        if(memSizeG<=1 ||memSizeG>=10){
            memSizeG=1;
        }
        //cpu 1000m是一个物理核心  250m表示是超分了4倍
        if(islimit)
            resourceRequirements.putLimitsItem("cpu",Quantity.fromString(cpuCount*250+"m")).putLimitsItem("memory",Quantity.fromString(memSizeG+"G"));
        master.setResources(resourceRequirements);

        masterContainer.add(master);
        v1PodSpec.setContainers(masterContainer);
        v1PodSpec.setRestartPolicy("Always");
        //设置pod的主机名
        v1PodSpec.setHostname(hostname);

        masterPod.setSpec(v1PodSpec);

        try {
            api.createNamespacedPod(nameSpace, masterPod, null, null, null);
        }catch (ApiException e){
//            System.out.println("创建Service失败");
//            System.out.println("Status code: {}"+e.getCode());
//            System.out.println("Reason: {}"+ e.getResponseBody());
//            System.out.println("Response headers: {}"+ e.getResponseHeaders());
            return e;
        }

        return null;

    }

    /****
     * 创建虚拟机 返回为null表示没有异常，创建成功！
     * @param nameSpace namespace要求全局唯一 会被转换成小写
     * @param imageURL 镜像地址
     * @param hostname 虚拟机的主机名 要求在namespace中是唯一的 作为其他的在同一个namespace中的主机访问的标识 会被转换成小写
     * @param portsMap 开放的端口映射
     * @param isExpose 是否开放给外部访问
     * @param islimit 是否限制内存和cpu，false表示不限制，设置false之后 cpucount和memsize将不起作用 true表示限制内存和cpu
     * @param cpuCount cpu的数量 小于1或者大于10 被设置成1
     * @param memSizeG 内存的大小 单位为G 小于1或者大于10 被设置成1
     * @param storageSizeG 存储的大小 单位G 小于1或者大于100 被设置成10
     * @return
     */
    public static ApiException createEndPoint(String nameSpace, String imageURL, String hostname, Map<String,Integer> portsMap,boolean isExpose,boolean islimit,int cpuCount,int memSizeG,int storageSizeG){

        CoreV1Api api=getAPI();
        nameSpace=nameSpace.toLowerCase();
        hostname=hostname.toLowerCase();

        //构建namespace对象
        V1Namespace namespace=new V1Namespace();
        namespace.setApiVersion(APIVersion);
        namespace.setKind(KIND_NAMESPACE);
        V1ObjectMeta meta=new V1ObjectMeta();
        meta.setName(nameSpace.toLowerCase());
        HashMap<String,String> map=new HashMap<>();
        //指定namespace的name 这个name需要是全局唯一的
        map.put("name",nameSpace.toLowerCase());
        meta.setLabels(map);
        namespace.setMetadata(meta);

        try{
            //如果namespace 不存在 就创建namespace
            if(!isNamespaceExists(nameSpace)) {
                api.createNamespace(namespace, null, null, null);
            }
        }catch (ApiException e){
//            System.out.println("创建 namespace 失败");
//            System.out.println("Status code: {}"+e.getCode());
//            System.out.println("Reason: {}"+ e.getResponseBody());
//            System.out.println("Response headers: {}"+ e.getResponseHeaders());
            return e;
        }

        //创建serivce
        V1Service service=new V1Service();

        service.setApiVersion(APIVersion);
        service.setKind(KIND_SERVICE);
        V1ObjectMeta serviceMeta=new V1ObjectMeta();
        serviceMeta.setName(hostname);
        service.setMetadata(serviceMeta);
        V1ServiceSpec serviceSpec=new V1ServiceSpec();
        HashMap<String,String> serviceSpecMap=new HashMap<>();
        serviceSpecMap.put("app",hostname);
        serviceSpec.setSelector(serviceSpecMap);
        List<V1ServicePort> ports=new ArrayList<>();

        //nodeport 是外部访问的链接 The range of valid ports is 30000-32767
        // 设置了targetPort 会自动分配端口
        // nodePort 使用指定的外部端口
        //isExpose 表示这个节点是否能够被外部访问
        if(isExpose) {
            serviceSpec.setType("NodePort");
            for (String  port :portsMap.keySet()) {
                ports.add(new V1ServicePort().name(port).port(portsMap.get(port)).targetPort(new IntOrString(portsMap.get(port))));
            }
        }else{
            serviceSpec.setClusterIP("None");
            for (String  port :portsMap.keySet()) {
                ports.add(new V1ServicePort().name(port).port(portsMap.get(port)));
            }
        }
        serviceSpec.setPorts(ports);
        service.setSpec(serviceSpec);

        try {
            api.createNamespacedService(nameSpace, service, null, null, null);
        }catch (ApiException e){
//            System.out.println("创建Service失败");
//            System.out.println("Status code: {}"+e.getCode());
//            System.out.println("Reason: {}"+ e.getResponseBody());
//            System.out.println("Response headers: {}"+ e.getResponseHeaders());
            return e;
        }


        //master 节点
        V1Pod masterPod=new V1Pod();
        masterPod.setApiVersion(APIVersion);
        masterPod.setKind(KIND_POD);
        V1ObjectMeta masterPodMeta=new V1ObjectMeta();
        masterPodMeta.setName(hostname);

        HashMap<String,String> masterPodMap=new HashMap<>();
        masterPodMap.put("app",hostname);
        masterPodMeta.setLabels(masterPodMap);
        masterPod.setMetadata(masterPodMeta);

        V1PodSpec v1PodSpec=new V1PodSpec();
        List<V1Container> masterContainer=new ArrayList<>();
        V1Container master=new V1Container();
        master.setImage(imageURL);
        master.setName(hostname);

        master.setImagePullPolicy("IfNotPresent");
        List<V1ContainerPort> v1ContainerPorts=new ArrayList<>();
        for (String  port :portsMap.keySet()) {
            v1ContainerPorts.add(new V1ContainerPort().containerPort(portsMap.get(port)));
        }
        master.setPorts(v1ContainerPorts);
        V1ResourceRequirements resourceRequirements=new V1ResourceRequirements();
        //设置pod的核心和内存大小
//        resourceRequirements.putRequestsItem("cpu", Quantity.fromString("1")).putRequestsItem("memory",Quantity.fromString("1G"));
        //设置pod的限制的核心和内存大小 如果cpucount 小于1或者大于10 则cpucount为1
        // memSizeG  小于1或者大于10 则memSizeG为1
        if(cpuCount<=1||cpuCount>=10){
            cpuCount=1;
        }
        if(memSizeG<=1 ||memSizeG>=10){
            memSizeG=1;
        }
        if(storageSizeG<=1|| storageSizeG>100){
            storageSizeG=10;
        }
        //cpu 1000m是一个物理核心  250m表示是超分了4倍
        if(islimit)
            resourceRequirements.putLimitsItem("cpu",Quantity.fromString(cpuCount*250+"m"))
                    .putLimitsItem("memory",Quantity.fromString(memSizeG+"G"))
                    .putLimitsItem("ephemeral-storage",Quantity.fromString(storageSizeG+"Gi"));
        master.setResources(resourceRequirements);

        masterContainer.add(master);
        v1PodSpec.setContainers(masterContainer);
        v1PodSpec.setRestartPolicy("Always");
        //设置pod的主机名
        v1PodSpec.setHostname(hostname);

        masterPod.setSpec(v1PodSpec);

        try {
            api.createNamespacedPod(nameSpace, masterPod, null, null, null);
        }catch (ApiException e){
//            System.out.println("创建Service失败");
//            System.out.println("Status code: {}"+e.getCode());
//            System.out.println("Reason: {}"+ e.getResponseBody());
//            System.out.println("Response headers: {}"+ e.getResponseHeaders());
            return e;
        }

        return null;

    }

    public static ApiException createGPUEndPoint(String nameSpace, String imageURL, String hostname, Map<String,Integer> portsMap,boolean isExpose,boolean islimit,int cpuCount,int memSizeG,int gpuCount,int storageSizeG){

        CoreV1Api api=getAPI();
        nameSpace=nameSpace.toLowerCase();
        hostname=hostname.toLowerCase();

        //构建namespace对象
        V1Namespace namespace=new V1Namespace();
        namespace.setApiVersion(APIVersion);
        namespace.setKind(KIND_NAMESPACE);
        V1ObjectMeta meta=new V1ObjectMeta();
        meta.setName(nameSpace.toLowerCase());
        HashMap<String,String> map=new HashMap<>();
        //指定namespace的name 这个name需要是全局唯一的
        map.put("name",nameSpace.toLowerCase());
        meta.setLabels(map);
        namespace.setMetadata(meta);

        try{
            //如果namespace 不存在 就创建namespace
            if(!isNamespaceExists(nameSpace)) {
                api.createNamespace(namespace, null, null, null);
            }
        }catch (ApiException e){
//            System.out.println("创建 namespace 失败");
//            System.out.println("Status code: {}"+e.getCode());
//            System.out.println("Reason: {}"+ e.getResponseBody());
//            System.out.println("Response headers: {}"+ e.getResponseHeaders());
            return e;
        }

        //创建serivce
        V1Service service=new V1Service();

        service.setApiVersion(APIVersion);
        service.setKind(KIND_SERVICE);
        V1ObjectMeta serviceMeta=new V1ObjectMeta();
        serviceMeta.setName(hostname);
        service.setMetadata(serviceMeta);
        V1ServiceSpec serviceSpec=new V1ServiceSpec();
        HashMap<String,String> serviceSpecMap=new HashMap<>();
        serviceSpecMap.put("app",hostname);
        serviceSpec.setSelector(serviceSpecMap);
        List<V1ServicePort> ports=new ArrayList<>();

        //nodeport 是外部访问的链接 The range of valid ports is 30000-32767
        // 设置了targetPort 会自动分配端口
        // nodePort 使用指定的外部端口
        //isExpose 表示这个节点是否能够被外部访问
        if(isExpose) {
            serviceSpec.setType("NodePort");
            for (String  port :portsMap.keySet()) {
                ports.add(new V1ServicePort().name(port).port(portsMap.get(port)).targetPort(new IntOrString(portsMap.get(port))));
            }
        }else{
            serviceSpec.setClusterIP("None");
            for (String  port :portsMap.keySet()) {
                ports.add(new V1ServicePort().name(port).port(portsMap.get(port)));
            }
        }
        serviceSpec.setPorts(ports);
        service.setSpec(serviceSpec);

        try {
            api.createNamespacedService(nameSpace, service, null, null, null);
        }catch (ApiException e){
//            System.out.println("创建Service失败");
//            System.out.println("Status code: {}"+e.getCode());
//            System.out.println("Reason: {}"+ e.getResponseBody());
//            System.out.println("Response headers: {}"+ e.getResponseHeaders());
            return e;
        }


        //master 节点
        V1Pod masterPod=new V1Pod();
        masterPod.setApiVersion(APIVersion);
        masterPod.setKind(KIND_POD);
        V1ObjectMeta masterPodMeta=new V1ObjectMeta();
        masterPodMeta.setName(hostname);

        HashMap<String,String> masterPodMap=new HashMap<>();
        masterPodMap.put("app",hostname);
        masterPodMeta.setLabels(masterPodMap);
        masterPod.setMetadata(masterPodMeta);

        V1PodSpec v1PodSpec=new V1PodSpec();
        List<V1Container> masterContainer=new ArrayList<>();
        V1Container master=new V1Container();
        master.setImage(imageURL);
        master.setName(hostname);

        master.setImagePullPolicy("IfNotPresent");
        List<V1ContainerPort> v1ContainerPorts=new ArrayList<>();
        for (String  port :portsMap.keySet()) {
            v1ContainerPorts.add(new V1ContainerPort().containerPort(portsMap.get(port)));
        }
        master.setPorts(v1ContainerPorts);
        V1ResourceRequirements resourceRequirements=new V1ResourceRequirements();
        //设置pod的核心和内存大小
//        resourceRequirements.putRequestsItem("cpu", Quantity.fromString("1")).putRequestsItem("memory",Quantity.fromString("1G"));
        //设置pod的限制的核心和内存大小 如果cpucount 小于1或者大于10 则cpucount为1
        // memSizeG  小于1或者大于10 则memSizeG为1
        if(cpuCount<=1||cpuCount>=10){
            cpuCount=1;
        }
        if(memSizeG<=1 ||memSizeG>=10){
            memSizeG=1;
        }
        //cpu 1000m是一个物理核心  250m表示是超分了4倍
        if(islimit)
            resourceRequirements.putLimitsItem("cpu",Quantity.fromString(cpuCount*250+"m"))
                    .putLimitsItem("memory",Quantity.fromString(memSizeG+"G"))
                    .putLimitsItem("ephemeral-storage",Quantity.fromString(storageSizeG+"Gi"));
        //增加gpu的调度
        resourceRequirements.putLimitsItem("nvidia.com/gpu",Quantity.fromString(gpuCount+""));
        master.setResources(resourceRequirements);

        masterContainer.add(master);
        v1PodSpec.setContainers(masterContainer);
        v1PodSpec.setRestartPolicy("Always");
        //设置pod的主机名
        v1PodSpec.setHostname(hostname);

        masterPod.setSpec(v1PodSpec);

        try {
            api.createNamespacedPod(nameSpace, masterPod, null, null, null);
        }catch (ApiException e){
//            System.out.println("创建Service失败");
//            System.out.println("Status code: {}"+e.getCode());
//            System.out.println("Reason: {}"+ e.getResponseBody());
//            System.out.println("Response headers: {}"+ e.getResponseHeaders());
            return e;
        }

        return null;

    }

    /***
     * 找到namespace下的pod  用来判断虚拟机是否正常
     * @param namespace
     * @param podName
     * @return
     */
    public static V1Pod getPod(String namespace,String podName){
        try{
            List<V1Pod> pods=  getAPI().listNamespacedPod(namespace.toLowerCase(),null,null,null,null,null,null, null,null,null,null).getItems();
            AtomicReference<V1Pod> pod= new AtomicReference<>(new V1Pod());
            pods.forEach(p->{
                if( p.getMetadata().getName().toLowerCase().equals(podName.toLowerCase())){
                    pod.set(p);
                }
            });
            return pod.get();
        }catch (ApiException ex){
            return null;
        }
    }

    /****
     * 找到namepspace下的service 用来获取虚拟机的访问方式
     * @param namespace
     * @param serviceName
     * @return
     */
    public static V1Service getService(String namespace,String serviceName){
        try{
            List<V1Service> pods=  getAPI().listNamespacedService(namespace.toLowerCase(),null,null,null,null,null,null, null,null,null,null).getItems();
            AtomicReference<V1Service> svc= new AtomicReference<>(new V1Service());
            pods.forEach(s->{
                if( s.getMetadata().getName().toLowerCase().equals(serviceName.toLowerCase())){
                    svc.set(s);
                }
            });
            return svc.get();
        }catch (ApiException ex){
            return null;
        }
    }

    /****
     * 删除namespace下的hostname 如果hostname被删除之后namespace下没有service和pod  namespace也会被删除
     * @param nameSpace
     * @param hostname
     * @return
     */

    public static ApiException deleteEndPoint(String nameSpace,String hostname){
        ApiException svcEx=deleteNameSpaceService(nameSpace,hostname);
        ApiException podEx=deleteNameSpacePod(nameSpace,hostname);
        ApiException nsEx= deleteNamespace(nameSpace,false);
        if(svcEx==null && podEx==null){
            return nsEx;
        }else{
            return svcEx==null?podEx:svcEx;
        }
    }

    /***
     * 判断pod是否是正常运行
     * @param pod
     * @return
     */
    public static boolean isPodRunning(V1Pod pod){
//        CrashLoopBackOff：容器退出，kubelet正在将它重启
//        InvalidImageName：无法解析镜像名称
//        ImageInspectError：无法校验镜像
//        ErrImageNeverPull：策略禁止拉取镜像
//        ImagePullBackOff：镜像正在重试拉取
//        RegistryUnavailable：连接不到镜像中心
//        ErrImagePull：通用的拉取镜像出错
//        CreateContainerConfigError：不能创建kubelet使用的容器配置
//        CreateContainerError：创建容器失败
//        m.internalLifecycle.PreStartContainer：执行hook报错
//        RunContainerError：启动容器失败
//        PostStartHookError：执行hook报错
//        ContainersNotInitialized：容器没有初始化完毕
//        ContainersNotReady：容器没有准备完毕
//        ContainerCreating：容器创建中
//        PodInitializing：pod 初始化中
//        DockerDaemonNotReady：docker还没有完全启动
//        NetworkPluginNotReady：网络插件还没有完全启动

        if(pod==null) return false;
        if(pod.getStatus()==null) return false;
        //如果ContainerStatuses 字段是null 表示pod不是正常运行状态
        if(pod.getStatus().getContainerStatuses()==null){
            return false;
        }
        return true;
    }

    /***
     * 获取节点的资源使用情况
     * cpuCount vCPU的数量 按照CPu进行了4倍超分
     * memorySize 内存数量
     * podCount 可以创建的pod的数量
     * podInUse 正在使用pod的数量
     * cpuInUse 正在使用的vcpu数量 没有限制vcpu的pod没有统计在内
     *  memoryInUse 正在使用内存的数量  没有限制内存使用的pod没有统计在内
     * @return
     */
    public static HashMap<String,Long> getNodeInfo(){
        CoreV1Api api=getAPI();
        HashMap<String,Long> hashMap=new HashMap<>();
        long cpuCount=0l;
        long memorySize=0l;
        long podCount=0l;
        long podInUse=0l;
        long cpuInUse=0l;
        long memoryInUse=0l;
        long ephemeralStorage=0l;
        long ephemeralStorageInUse=0l;
        try {

            V1NodeList nodeList= api.listNode(null,null,null,null,null,null,null,null,null,null);
            for(V1Node node:nodeList.getItems()){

                cpuCount+= node.getStatus().getCapacity().get("cpu").getNumber().longValue()*4;
                memorySize+=node.getStatus().getCapacity().get("memory").getNumber().longValue();
                podCount+=node.getStatus().getCapacity().get("pods").getNumber().longValue();
                ephemeralStorage+=node.getStatus().getCapacity().get("ephemeral-storage").getNumber().longValue() ;//ephemeral-storage
            }
            V1PodList podList= api.listPodForAllNamespaces(null,null,null,null,null,null,null,null,null,null);
            for(V1Pod pod :podList.getItems()){
                if(!isPodRunning(pod))continue;
                podInUse+=1;
                for(V1Container container:pod.getSpec().getContainers()) {
                    //limit的优先级高于request的优先级，优先统计limit占用的资源
                    //cpu的数量可能是小数
                    if(container.getResources().getLimits()==null){
                        if(container.getResources().getRequests()!=null){
                            if( container.getResources().getRequests().get("cpu")!=null){
                                cpuInUse+=container.getResources().getRequests().get("cpu").getNumber().floatValue()*4;
                            }
                            if( container.getResources().getRequests().get("memory")!=null){
                                memoryInUse+=container.getResources().getRequests().get("memory").getNumber().longValue();
                            }
                            if( container.getResources().getRequests().get("ephemeral-storage")!=null){
                                ephemeralStorageInUse+=container.getResources().getRequests().get("ephemeral-storage").getNumber().longValue();
                            }
                        }
                    }else{
                        if(container.getResources().getLimits().get("cpu")!=null){
                            cpuInUse+=container.getResources().getLimits().get("cpu").getNumber().floatValue()*4;
                        }
                        if( container.getResources().getLimits().get("memory")!=null){
                            memoryInUse+=container.getResources().getLimits().get("memory").getNumber().longValue();
                        }
                        if( container.getResources().getRequests().get("ephemeral-storage")!=null){
                            ephemeralStorageInUse+=container.getResources().getRequests().get("ephemeral-storage").getNumber().longValue();
                        }
                    }
                }
            }



        }catch (ApiException ex){

        }
        hashMap.put("cpuCount",cpuCount);
        //vCPU的数量 按照CPu进行了4倍超分
        hashMap.put("memorySize",memorySize);
        //内存数量
        hashMap.put("podCount",podCount);
        //可以创建的pod的数量
        hashMap.put("podInUse",podInUse);
        //正在使用pod的数量
        hashMap.put("cpuInUse",cpuInUse);
        //正在使用的vcpu数量 没有限制vcpu的pod没有统计在内
        hashMap.put("memoryInUse",memoryInUse);
        //正在使用内存的数量  没有限制内存使用的pod没有统计在内
        hashMap.put("ephemeralStorage",ephemeralStorage);
        // 总的存储的数量
        hashMap.put("ephemeralStorageInUse",ephemeralStorageInUse);
        //已经被使用的存储数量

        return hashMap;
    }

    public static void getImages()  {
        CoreV1Api api = getAPI();
        try {
            V1NodeList nodeList = api.listNode(null, null, null, null, null, null, null, null, null, null);
            System.out.println("size -----> " + nodeList.getItems().size());
            for (V1Node node : nodeList.getItems()) {
//                node.getStatus().getConditions().
//                System.out.println(node.getStatus().getImages().toString());
                node.getStatus().getImages().forEach(s->System.out.println(s.getNames().toString()));
//                break;
            }
        }catch (ApiException ex){

        }
    }
    public static void handupEndpoint(){
        // org案例 [{"op": "replace", "path": "/spec/containers/0/image", "value":"new image"}]

        V1Patch patch=new V1Patch("[{\"op\": \"delete\", \"path\": \"/spec/containers/0/resources/limits\"}," +
                "{\"op\": \"replace\", \"path\": \"/metadata/name\",\"value\":\"master-handup\"}]");

        patch=new V1Patch("[{ \"op\": \"replace\", \"path\": \"/metadata/labels/app\", \"value\": \"master-handup\" }]");

        CoreV1Api api=getAPI();
        try {
            api.patchNamespacedService("master", "user-0", patch, null, null, null, null);
            V1Pod pod=  getPod("user-0","master");
//          pod.getMetadata().name("master-handup");
            pod.getSpec().getContainers().get(0).getResources().getLimits().clear();
//            api.replaceNamespacedPod("master","user-0",pod,null,null,null);

        }catch (ApiException e){
            System.out.println("Status code: {}"+e.getCode());
            System.out.println("Reason: {}"+ e.getResponseBody());
            System.out.println("Response headers: {}"+ e.getResponseHeaders());
        }
    }

    public static void main(String[] args) {
        String imgeurl="192.168.8.99:5000/mhys/hadoop:latest";
        //192.168.18.9:30050/library/hadoop:v1
        HashMap<String,Integer> ports=new HashMap<>();
        ports.put("ssh",22);
        ports.put("ide",8080);
        ports.put("hdfs",9000);
        ports.put("yarn",18808);
        ports.put("hadoop",50070);
        String user="mhys-ai-";
//        getImages();
//        createGPUEndPoint("gpu-test","192.168.18.9:30050/library/msgpu:all","master",ports,true,true,2,4,1,10);
//        deleteEndPoint("gpu-test","master");
        for(int i=0;i<2;i++) {

//            createEndPoint(user+"hzp-"+i,imgeurl,"master",ports,true,true,1,2,10);
            deleteEndPoint(user+"hzp-"+i,"master");
//            createEndPoint(user+i,imgeurl,"slave1",ports,true,false,1,2);
//            createEndPoint(user+i,imgeurl,"slave2",ports,true,false,1,2);
//            deleteEndPoint(user+i,"master");
//            deleteEndPoint(user+i,"slave1");
//            deleteEndPoint(user+i,"slave2");
//            System.out.println(isPodRunning( getPod(user+i,"master"))?"Running":"NoRunning");
//            System.out.println(getService(user+i,"master"));


        }
//       System.out.println( getNodeInfo());
//        handupEndpoint();;


    }
}

