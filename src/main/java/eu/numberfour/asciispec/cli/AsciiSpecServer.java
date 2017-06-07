package eu.numberfour.asciispec.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import org.asciidoctor.cli.AsciidoctorInvoker;

public class AsciiSpecServer {
	static final int PORT = 45115;
	static final String ENCODING = "UTF-8";

	public AsciiSpecServer() {
	}

	public static void main(String[] args) throws IOException {
		int port = getPort(args);
		ServerSocket socket = new ServerSocket(port);
		new AsciiSpecServer().run(socket);
	}

	private static int getPort(String[] args) {
		if ((args != null) && (args.length > 0)) {
			try {
				int portArg = Integer.parseInt(args[0]);
				if (portArg > 0) {
					return portArg;
				}
			} catch (Throwable localThrowable) {
			}
		}
		return 45115;
	}

	PrintStream sysOut = System.out;

	private void run(ServerSocket socket) {
		sysOut.println("Asciispec Server started on port " + socket.getLocalPort());
		while (!Thread.interrupted()) {
			try {
				Socket client = socket.accept();
				sysOut.println("Asciispec Server: Client accepted");
				BufferedReader br = setupClient(client);
				handleClient(client, br);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		sysOut.println("Asciispec Server terminated.");
	}

	private BufferedReader setupClient(Socket client) throws IOException, java.io.UnsupportedEncodingException {
		OutputStream os = client.getOutputStream();
		System.setOut(new PrintStream(os));
		System.setErr(new PrintStream(os));
		InputStream is = client.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		return br;
	}

	private void handleClient(Socket client, BufferedReader br) {
		try {
			String line = br.readLine();
			System.out.println("asciispecSv: INFO: Job received: " + line);
			String[] args = getArgs(line);
			AsciidoctorInvoker.main(args);
			System.out.println("asciispecSv: INFO: Job finished");
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		sysOut.println("Asciispec Server: Client finished.");
	}

	private String[] getArgs(String line) {
		String[] args = line.split("\\s");
		args = consumeWorkingDir(args);
		return args;
	}

	private String[] consumeWorkingDir(String[] args) {
		List<String> newArgs = new LinkedList<>();
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-B")) {
				setWorkingDir(args[(i + 1)]);
				i += 2;
			}
			if (i < args.length) {
				newArgs.add(args[i]);
			}
		}
		return newArgs.toArray(new String[newArgs.size()]);
	}

	private void setWorkingDir(String string) {
		System.setProperty("user.dir", string);
	}
}

