package org.openmrs.loadtests;
import io.gatling.recorder.GatlingRecorder;
import io.gatling.recorder.config.RecorderPropertiesBuilder;

import scala.Option;
import scala.Some;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Recorder {
	public static void main(String[] args) {
		RecorderPropertiesBuilder props = new RecorderPropertiesBuilder()
				.simulationsFolder(IDEPathHelper.recorderSimulationsDirectory().toString())
				.resourcesFolder(IDEPathHelper.resourcesDirectory().toString())
				.simulationPackage("com.example.simulation");
		
		GatlingRecorder.fromMap(props.build(), Option.apply(IDEPathHelper.recorderConfigFile()));
	}
}

class IDEPathHelper {
	private static final String ROOT_DIR = System.getProperty("user.dir");
	
	public static Path recorderSimulationsDirectory() {
		return Paths.get(ROOT_DIR, "src", "test", "java");
	}
	
	public static Path resourcesDirectory() {
		return Paths.get(ROOT_DIR, "src", "main", "resources");
	}
	
	public static Path recorderConfigFile() {
		return resourcesDirectory().resolve("recorder.conf");
	}
}
