# AnomalyDetection算法调试过程

## 背景

​	因本人学习过程中接触到aiops这一块的知识，于是想做一个案例用于检验学习成果。有幸了解到阿里SREWorks团队的开源项目，感觉与自己的想法方向十分吻合，在炯思大佬的指导之下成功将该算法拉起，并作为后续调试的基石。现将AnomalyDetection算法本地拉起调试过程，及遇到的问题做一个整理。

## 过程

### 1、算法路径

​	https://github.com/alibaba/SREWorks/tree/main/saas/aiops/api/anomalydetection

​	将该路径下文件夹全部上传至个人服务器 /opt/ 路径下

### 2、bentoml环境安装

 	cd /opt/anomalydetection/ 执行以下命令

​	pip3 install -r requirements.txt -i <http://mirrors.aliyun.com/pypi/simple/> --trusted-host mirrors.aliyun.com

### 3、服务启动

​	python3 -m bentoml serve ./AnomalyDetection

### 4、调试结果

```
调用地址及参数：

curl -X POST -k 'http://127.0.0.1:55119/analyze' \
> -H 'accept: */*' \
> -H 'Content-Type: application/json' \
> -d '{"taskType":"sync","series":[[1641225600,15391.1337890625],[1641225660,17223.55078125],[1641225720,15635.00390625],[1641225780,16146.3125],[1641225840,15914.775390625],[1641225900,15924.4794921875],[1641225960,15791.970703125],[1641226020,15637.287109375],[1641226080,15546.31640625],[1641226140,15490.6123046875],[1641226200,15250.00390625],[1641226260,15079.779296875],[1641226320,14856.2958984375],[1641226380,14932.2587890625],[1641226440,14805.875],[1641226500,14736.8876953125],[1641226560,14527.7705078125],[1641226620,14354.75390625],[1641226680,14310.7373046875],[1641226740,14398.283203125],[1641226800,14188.2705078125],[1641226860,14047.7919921875],[1641226920,13847.2412109375],[1641226980,13849.4912109375],[1641227040,13716.3876953125],[1641227100,13727.875],[1641227160,13450.43359375],[1641227220,13364.375],[1641227280,13340.783203125],[1641227340,13295.099609375],[1641227400,13307.30859375],[1641227460,13116.8916015625],[1641227520,12910.0830078125],[1641227580,12960.095703125],[1641227640,12862.2373046875],[1641227700,12862.5498046875],[1641227760,12657.1455078125],[1641227820,12521.779296875],[1641227880,12514.9501953125],[1641227940,12516.19140625],[1641228000,12446.8876953125],[1641228060,12372.3037109375],[1641228120,12127.68359375],[1641228180,12173.337890625],[1641228240,12059.5751953125],[1641228300,12062.662109375],[1641228360,11919.7705078125],[1641228420,11726.650390625],[1641228480,11739.5205078125],[1641228540,11717.7587890625],[1641228600,11590.25390625],[1641228660,11563.7705078125],[1641228720,11325.775390625],[1641228780,11310.12890625],[1641228840,11256.224609375],[1641228900,11374.6044921875],[1641228960,11158.8876953125],[1641229020,11025.8330078125],[1641229080,10919.87890625],[1641229140,10904.4375],[1641229200,10396.154296875],[1641229260,11365.5498046875],[1641229320,10653.4873046875],[1641229380,10631.37890625],[1641229440,10604.5458984375],[1641229500,10698.5458984375],[1641229560,10521.904296875],[1641229620,10349.93359375],[1641229680,10331.25390625],[1641229740,10329.3916015625],[1641229800,10325.1669921875],[1641229860,10216.712890625],[1641229920,571.18359375]],"algoParam":{"returnBounds":true}}'

返回值：
{"data": {"detectSeries": [[1641229920.0, 571.18359375, 1.0, 18399.025268540678, 7440.170288099947]], "detectSeriesColumns": ["timestamp", "value", "anomaly", "upperbound", "lowerbound"]}}


```

​	**所传入的参数为项目中的提供的测试数据，在AnomalyDetection/mock.json中**

## 遇到的问题

### 1、python环境

​	本算法python_version 为3.7.10，阿里云服务器默认安装python2，而python2和python3不兼容，所以还需要额外安装python3。

### 2、pip3: command not found

​	原因（1）：可能是pip3没有安装

​	解决方法：安装pip3

```
sudo apt install python3-pip
```

​	原因（2）：安装python3的时候将pip3软连接到了pip上

​	解决方案：将调用命令改为pip即可

### 3、服务启动后调用接口返回为页面

​	错误描述：服务启动之后，利用curl传参，服务器返回html页面

​	返回代码：

```
HTTP/1.1 100 Continue

HTTP/1.0 200 OK
Server: Werkzeug/2.2.2 Python/3.9.9
Date: Thu, 27 Oct 2022 07:57:29 GMT
Content-Type: text/html; charset=utf-8
Content-Length: 2019
Connection: close

<!DOCTYPE html>
<head>
  <link rel="stylesheet" type="text/css" href="static_content/main.css">
  <link rel="stylesheet" type="text/css" href="static_content/readme.css">
  <link rel="stylesheet" type="text/css" href="static_content/swagger-ui.css">
</head>
<body>
  <div id="tab">
    <button
      class="tabLinks active"
      onclick="openTab(event, 'swagger_ui_container')"
      id="defaultOpen"
    >
      Swagger UI
    </button>
    <button class="tabLinks" onclick="openTab(event, 'markdown_readme')">
      ReadMe
    </button>
  </div>
  <script>
    function openTab(evt, tabName) {
      // Declare all variables
      var i, tabContent, tabLinks;
      // Get all elements with class="tabContent" and hide them
      tabContent = document.getElementsByClassName("tabContent");
      for (i = 0; i < tabContent.length; i++) {
        tabContent[i].style.display = "none";
      }

      // Get all elements with class="tabLinks" and remove the class "active"
      tabLinks = document.getElementsByClassName("tabLinks");
      for (i = 0; i < tabLinks.length; i++) {
        tabLinks[i].className = tabLinks[i].className.replace(" active", "");
      }

      // Show the current tab, and add an "active" class to the button that opened the
      // tab
      document.getElementById(tabName).style.display = "block";
      evt.currentTarget.className += " active";
    }
  </script>
  <div id="markdown_readme" class="tabContent"></div>
  <script src="static_content/marked.min.js"></script>
  <script>
    var markdownContent = marked(`
    A minimum prediction service exposing a Scikit-learn model
    `);
    var element = document.getElementById('markdown_readme');
    element.innerHTML = markdownContent;
  </script>
  <div id="swagger_ui_container" class="tabContent" style="display: block"></div>
  <script src="static_content/swagger-ui-bundle.js"></script>
  <script>
      SwaggerUIBundle({
          url: '/docs.json',
          dom_id: '#swagger_ui_container'
      })
  </script>
</body>

```

​	这里所返回的是最原始的服务接口，项目是由aiops做了一个接口网关，然后将这个接口又代理了一层。如果是为了验证接口能力，可以直接在返回的 swagger 给的接口中直接调用。

​	如果想通过curl传参调用算法，按照过程（4）参数格式调用即可。

## 最后

​	再次感谢炯思大佬在百忙之中抽出时间对我细心指导，也衷心希望阿里SREWorks团队越来越好！

