## StormCV
StormCV enables the use of [Apache Storm](https://storm.apache.org/) for video processing by adding computer vision (CV) specific operations and data model. The platform enables the development of distributed video processing pipelines which can be deployed on Storm clusters. The screenshot below is taken from a distributed pipeline that calculates SIFT and SURF features in real time on six live streams. 
![example output](https://github.com/sensorstorm/StormCV/wiki/deployed_output.png)
Please see the [Wiki](https://github.com/sensorstorm/StormCV/wiki) for more information on the platform and pointers how to get started.


## How to Compile and Execute
**Compiling StormCV**

cd into the StormCV/stormcv directory and run "mvn clean install -DskipTests=true -X"

**Executing a topology on Storm**

* From the StormCV/stormcv directory, run "mvn compile exec:java -Dstorm.topology=nl.tno.stormcv.[topology name]"
* For example: "mvn compile exec:java -Dstorm.topology=nl.tno.stormcv.E9_ContrastEnhancementTopology" will run the Contrast Enhancement Topology.
* Available Topologies are: 
    * E1_GrayScaledTopology
    * E4_SequentialFeaturesTopology
    * E7_FetchOperateCombiTopology
    * E2_FacedetectionTopology
    * E5_TilingTopology
    * E8_BackgroundSubtractionTopology
    * E3_MultipleFeaturesTopology	
    * E6_GroupOfFramesTopology
    * E9_ContrastEnhancementTopology

Note: The Topologies are set to run on the LocalCluster and will time out after 2 minutes. To modify this behavior, tweak the code in main function of each topology class.

## How to see the output
* StormCV creates its own simple webservice used to view results MJPEG streams. Once the topology is running, execute one of the following calls by 
typing in the url into the browser:
 * http://IP:PORT/streaming/streams : lists the available JPG and MJPEG urls
 * http://IP:PORT/streaming/picture/{streamid}.jpg : url to grab jpg pictures 
 * http://IP:PORT/streaming/tiles : provides a visual overview of all the streams available at this service. Clicking an image will open the mjpeg stream
 * http://IP:PORT/streaming/mjpeg/{streamid}.mjpeg : provides a possibly never ending mjpeg formatted stream

Make sure to replace the "IP" with your network IP and use "8558" for Port. For example, my computer has IP of 10.0.0.9, so I type in "http://10.0.0.9:8558/streaming/tiles" to view the output streams :)


## Demo on Youtube 
[https://youtu.be/p3Xm31XW4OE](https://youtu.be/p3Xm31XW4OE)

## License
Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

**OpenCV License**

Also see the [OpenCV License](http://opencv.org/license.html). Please note that the OpenCV build also contains the [non-free modules](http://docs.opencv.org/modules/nonfree/doc/nonfree.html)!
