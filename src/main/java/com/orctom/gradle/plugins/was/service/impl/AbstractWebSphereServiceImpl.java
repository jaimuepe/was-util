package com.orctom.gradle.plugins.was.service.impl;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.orctom.gradle.plugins.was.model.Command;
import com.orctom.gradle.plugins.was.model.WebSphereModel;
import com.orctom.gradle.plugins.was.model.WebSphereServiceException;
import com.orctom.gradle.plugins.was.service.IWebSphereService;
import com.orctom.gradle.plugins.was.utils.CommandUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Using jython
 * Created by CH on 3/11/14.
 */
public abstract class AbstractWebSphereServiceImpl implements IWebSphereService {

	private WebSphereModel model;
	private String workingDir;

	private static final String TEMPLATE = "jython/websphere.py";
	private static final String TEMPLATE_EXT = "py";

	public AbstractWebSphereServiceImpl(WebSphereModel model, String targetDir) {
		this.model = model;
		this.workingDir = CommandUtils.getWorkingDir(targetDir, TEMPLATE_EXT);
	}

	public void restartServer() {
		execute("restartServer");
	}

	public void startServer() {
		execute("startServer");
	}

	public void stopServer() {
		execute("stopServer");
	}

	public void installApplication() {
		execute("installApplication");
	}

	public void uninstallApplication() {
		execute("uninstallApplication");
	}

	public void startApplication() {
		execute("startApplication");
	}

	public void stopApplication() {
		execute("stopApplication");
	}

	public void deploy() {
		execute("deploy");
	}

	protected final void execute(String task) {
		try {
			Command command = getCommand(task);
			if (model.isVerbose()) {
				System.out.println(model.toString());
			}
			executeCommand(command);
		} catch (Exception e) {
			throw new WebSphereServiceException(e.getMessage(), e);
		}
	}

	protected Command getCommand(String task) throws IOException {
		File buildScript = CommandUtils.getBuildScript(task, TEMPLATE, model, workingDir, TEMPLATE_EXT);
		Command command = new Command();
		command.setExecutable(CommandUtils.getExecutable(model.getWasHome(), "wsadmin").getAbsolutePath());
		command.setWorkingDir(workingDir);
		command.addArg("-conntype", model.getConnectorType());
		command.addArg("-host", model.getHost());
		command.addArg("-port", model.getPort());
		command.addArg("-port", model.getPort());
		if (!Strings.isNullOrEmpty(model.getUser())) {
			command.addArg("-user", model.getUser());
			if (!Strings.isNullOrEmpty(model.getPassword())) {
				command.addArg("-password", model.getPassword());
			}
		}

		command.addArg("-lang", "jython");
		if (!Strings.isNullOrEmpty(model.getJavaoption())) {
			for (String option : Splitter.on(" ").split(model.getJavaoption())) {
				command.addArg("-javaoption", option);
			}
		}
		command.addArg("-tracefile", buildScript.getAbsolutePath() + ".trace");
		command.addArg("-appendtrace", "true");
		command.addArg("-f", buildScript.getAbsolutePath());
		if (!Strings.isNullOrEmpty(model.getScript()) && !Strings.isNullOrEmpty(model.getScriptArgs())) {
			for (String scriptArg : Splitter.on(" ").split(model.getScriptArgs())) {
				command.addArg(scriptArg, null);
			}
		} else {
			command.addArg("-o ", task);
		}
		return command;
	}

	protected abstract void executeCommand(Command command);

}
