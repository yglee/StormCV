package nl.tno.stormcv;

import java.util.ArrayList;
import java.util.List;

import nl.tno.stormcv.StormCVConfig;
import nl.tno.stormcv.batcher.SequenceNrBatcher;
import nl.tno.stormcv.batcher.SlidingWindowBatcher;
import nl.tno.stormcv.bolt.BatchInputBolt;
import nl.tno.stormcv.bolt.SingleInputBolt;
import nl.tno.stormcv.example.util.DummyTileGrouping;
import nl.tno.stormcv.fetcher.StreamFrameFetcher;
import nl.tno.stormcv.model.Frame;
import nl.tno.stormcv.model.serializer.FrameSerializer;
import nl.tno.stormcv.operation.GrayscaleOp;
import nl.tno.stormcv.operation.MjpegStreamingOp;
import nl.tno.stormcv.operation.TilesRecombinerOp;
import nl.tno.stormcv.operation.TilingOp;
import nl.tno.stormcv.spout.CVParticleSpout;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import backtype.storm.utils.Utils;

public class E5_TilingTopology {

	public static void main(String[] args){
		// first some global (topology configuration)
		StormCVConfig conf = new StormCVConfig();
		conf.setNumWorkers(6); // number of workers in the topology
		conf.setMaxSpoutPending(32); // maximum un-acked/un-failed frames per spout (spout blocks if this number is reached)
		conf.put(StormCVConfig.STORMCV_FRAME_ENCODING, Frame.JPG_IMAGE); // indicates frames will be encoded as JPG throughout the topology (JPG is the default when not explicitly set)
		conf.put(Config.TOPOLOGY_ENABLE_MESSAGE_TIMEOUTS, true); // True if Storm should timeout messages or not.
		conf.put(Config.TOPOLOGY_MESSAGE_TIMEOUT_SECS , 10); // The maximum amount of time given to the topology to fully process a message emitted by a spout (default = 30)
		conf.put(StormCVConfig.STORMCV_SPOUT_FAULTTOLERANT, false); // indicates if the spout must be fault tolerant; i.e. spouts do NOT! replay tuples on fail
		conf.put(StormCVConfig.STORMCV_CACHES_TIMEOUT_SEC, 30); // TTL (seconds) for all elements in all caches throughout the topology (avoids memory overload)
		
		List<String> urls = new ArrayList<String>();
		urls.add( "rtsp://streaming3.webcam.nl:1935/n224/n224.stream" );
		//urls.add("rtsp://streaming3.webcam.nl:1935/n233/n233.stream");

		int frameSkip = 13;
		
		// now create the topology itself (spout -> scale --> grayscale --> streamer)
		TopologyBuilder builder = new TopologyBuilder();
		
		// just one spout reading streams; i.e. this spout reads two streams in parallel
		builder.setSpout("spout", new CVParticleSpout( new StreamFrameFetcher(urls).frameSkip(frameSkip) ), 1 );
		
		// splits each frame into 4 tiles (2x2) with 0 pixels overlap
		builder.setBolt("tiler", new SingleInputBolt(new TilingOp(2, 2).overlap(0)), 1).shuffleGrouping("spout");
		
		// execute grayscale operation only on tiles 1 and 2 (top right and bottom left)
		builder.setBolt("grayscale", new SingleInputBolt(new GrayscaleOp()), 1)
			.customGrouping("tiler", new DummyTileGrouping(new String[]{"1", "2"})); // the custom grouping used filters on tiles numbers 1 and 2
		
		// the tile merger stitches the tiles back together and more importantly also merges features created for each tile.
		// the result of the merger is almost the same as if the tiling did not take place 
		builder.setBolt("tile_merger", new BatchInputBolt(new SequenceNrBatcher(4), new TilesRecombinerOp().outputFrame(true))
			.groupBy(new Fields(FrameSerializer.SEQUENCENR)), 1)
			.shuffleGrouping("grayscale") // gets gray tiles numbers 1 and 2 from the grayscale bolt
			.customGrouping("tiler", new DummyTileGrouping(new String[]{"0", "3"})); // gets full color tiles 0 and 3 direclty from the tiler bolt

		// add bolt that creates a webservice on port 8558 enabling users to view the result
		builder.setBolt("streamer", new BatchInputBolt(
				new SlidingWindowBatcher(2, frameSkip).maxSize(6), // note the required batcher used as a buffer and maintains the order of the frames
				new MjpegStreamingOp().port(8558).framerate(5)).groupBy(new Fields(FrameSerializer.STREAMID))
			, 1)
			.shuffleGrouping("tile_merger");
		
		try {
			
			// run in local mode
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology( "tiling_ftw", conf, builder.createTopology() );
			Utils.sleep(120*1000); // run for one minute and then kill the topology
			cluster.shutdown();
			System.exit(1);
			
			// run on a storm cluster
			// StormSubmitter.submitTopology("some_topology_name", conf, builder.createTopology());
		} catch (Exception e){
			e.printStackTrace();
		}
	}

}
