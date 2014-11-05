package amazon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairRequest;
import com.amazonaws.services.ec2.model.CreateKeyPairResult;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.KeyPair;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.StartInstancesRequest;
import com.amazonaws.services.ec2.model.StartInstancesResult;
import com.amazonaws.services.ec2.model.StopInstancesRequest;
import com.amazonaws.services.ec2.model.StopInstancesResult;

public class AmazonConnector {

	AmazonEC2Client amazonEC2Client = null;
	String securityGroupName = "Java Security Group Image server";
	KeyPair amazonKey = null;
	ArrayList<String> instanceIDsStrings = (ArrayList<String>) Arrays
			.asList(new String[] { "i-f71ee4fb", "i-f01ee4fc", "i-f11ee4fd",
					"i-f21ee4fe", "i-f31ee4ff" });
	String keyPairName = "amazon775";

	/**
	 * connect to amazon cloud, must have an amazon.properties file as input and
	 * a keyname for the instances
	 * 
	 * starts up one machine
	 * 
	 * @param propertiesFile
	 *            (example file: /home/user/amazon.properties)
	 * @param keyName
	 *            the keyname to use with the instances
	 */
	public AmazonConnector(File propertiesFile, String keyName) {
		AWSCredentials credentials = null;
		try {
			credentials = new PropertiesCredentials(propertiesFile);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		if (credentials != null) {
			amazonEC2Client = new AmazonEC2Client(credentials);
			amazonEC2Client.setEndpoint("ec2.us-west-2.amazonaws.com");
			System.out.println("Created Amazon client, ready to run instances");
		} else {
			System.err.println("Couldn't create Amazon client");
			System.err.println("Quitting now");
			System.exit(0);
		}
		System.out.println("Creating one instance at start");
		// createSecurityGroup(securityGroupName,
		// "a security group for image_server with SSH enabled");
		amazonKey = createKeyPair(keyName);
	}

	public AmazonConnector(File propertiesFile) {
		AWSCredentials credentials = null;
		try {
			credentials = new PropertiesCredentials(propertiesFile);
		} catch (IOException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		if (credentials != null) {
			amazonEC2Client = new AmazonEC2Client(credentials);
			amazonEC2Client.setEndpoint("ec2.us-west-2.amazonaws.com");
			System.out
					.println("Created Amazon client, ready to start instances...");
		} else {
			System.err.println("Couldn't create Amazon client");
			System.err.println("Quitting now");
			System.exit(0);
		}
	}

	public String getSecurityGroupName() {
		return securityGroupName;
	}

	public void setSecurityGroupName(String securityGroupName) {
		this.securityGroupName = securityGroupName;
	}

	public AmazonEC2Client getAmazonEC2Client() {
		return amazonEC2Client;
	}

	public KeyPair getAmazonKey() {
		return amazonKey;
	}

	/**
	 * creates a securitygroup with SSH access
	 * 
	 * @param groupName
	 *            , name of the security group
	 * @param description
	 *            , description of the security group
	 */
	private void createSecurityGroup(String groupName, String description) {
		CreateSecurityGroupRequest createSecurityGroupRequest = new CreateSecurityGroupRequest();

		createSecurityGroupRequest.withGroupName(groupName).withDescription(
				description);
		try {
			amazonEC2Client.createSecurityGroup(createSecurityGroupRequest);
		} catch (AmazonServiceException aSException) {
			System.err.println(aSException.getMessage());
			aSException.printStackTrace();
		} catch (AmazonClientException aCException) {
			System.err.println(aCException.getMessage());
			aCException.printStackTrace();
		}
		System.out.println("Security group made, setting ingress...");
		setSecurityGroupIngress(groupName);
	}

	/**
	 * sets up the way to ingress the instance according to the securitygroup's
	 * ingress this sets up SSH access to the instances
	 * 
	 * @param groupName
	 *            , the name of the securitygroup
	 */
	private void setSecurityGroupIngress(String groupName) {
		IpPermission ipPermission = new IpPermission();

		// set up SSH access in the TU Delft (Starts with 145.94)
		ipPermission.withIpRanges("145.94.0.0/32", "145.94.255.255/32")
				.withIpProtocol("tcp").withFromPort(22).withToPort(22);

		AuthorizeSecurityGroupIngressRequest authorizeSecurityGroupIngressRequest = new AuthorizeSecurityGroupIngressRequest();

		authorizeSecurityGroupIngressRequest.withGroupName(groupName)
				.withIpPermissions(ipPermission);

		try {
			amazonEC2Client
					.authorizeSecurityGroupIngress(authorizeSecurityGroupIngressRequest);
		} catch (AmazonServiceException aSException) {
			System.err.println(aSException.getMessage());
			aSException.printStackTrace();
		} catch (AmazonClientException aCException) {
			System.err.println(aCException.getMessage());
			aCException.printStackTrace();
		}

		System.out.println("Security ingress set");
	}

	/**
	 * creates the keypair given from a .pem file and returns it
	 * 
	 * @param keyName
	 * @return keypair, or null if unsuccessful
	 */
	private KeyPair createKeyPair(String keyName) {
		CreateKeyPairRequest createKeyPairRequest = new CreateKeyPairRequest();

		createKeyPairRequest.withKeyName(keyName);

		CreateKeyPairResult createKeyPairResult = null;
		try {
			createKeyPairResult = amazonEC2Client
					.createKeyPair(createKeyPairRequest);
			KeyPair keyPair = new KeyPair();
			keyPair = createKeyPairResult.getKeyPair();
			System.out.println("KeyPair created");
			return keyPair;
		} catch (AmazonServiceException aSException) {
			System.err.println(aSException.getMessage());
			aSException.printStackTrace();
		} catch (AmazonClientException aCException) {
			System.err.println(aCException.getMessage());
			aCException.printStackTrace();
		}
		return null;
	}

	/**
	 * start the instances with certain IDs
	 * 
	 * @param instanceID
	 * @return true if succeeded
	 */
	public boolean startInstances(String[] instanceIDCollection) {
		StartInstancesRequest startInstancesRequest = new StartInstancesRequest();
		startInstancesRequest.withInstanceIds(instanceIDCollection);
		try {
			StartInstancesResult response = amazonEC2Client
					.startInstances(startInstancesRequest);
			System.out.println("Sent! " + response.toString());
		} catch (AmazonServiceException ex) {
			System.out.println(ex.toString());
			return false;
		} catch (AmazonClientException ex) {
			System.out.println(ex.toString());
			return false;
		}
		return true;
	}
	
	/**
	 * stop the instances with certain IDs
	 * 
	 * @param instanceID
	 * @return true if succeeded
	 */
	public boolean stopInstances(String[] instanceIDCollection) {
		StopInstancesRequest stopInstancesRequest = new StopInstancesRequest();
		stopInstancesRequest.withInstanceIds(instanceIDCollection);
		try {
			StopInstancesResult response = amazonEC2Client
					.stopInstances(stopInstancesRequest);
			System.out.println("Sent! " + response.toString());
		} catch (AmazonServiceException ex) {
			System.out.println(ex.toString());
			return false;
		} catch (AmazonClientException ex) {
			System.out.println(ex.toString());
			return false;
		}
		return true;
	}

	/**
	 * run new instances
	 * @param amountOfInstances
	 */
	public void runInstances(int amountOfInstances) {
		System.out.println("Starting " + amountOfInstances + " instances");
		RunInstancesRequest runInstancesRequest = new RunInstancesRequest();

		// TODO: convert this publicly available AMI to a private one we made
		// ami-8b024dbb is for XLT-4.4.3 java 7 ubuntu 14.04 -64 bit image
		runInstancesRequest.withImageId("ami-13fbb723")
				.withInstanceType("t2.micro").withMinCount(amountOfInstances)
				.withMaxCount(amountOfInstances)
				.withKeyName(amazonKey.getKeyName())
				.withSecurityGroups(securityGroupName);

		RunInstancesResult result = amazonEC2Client
				.runInstances(runInstancesRequest);
		System.out.println(result.toString());
		System.out.println(amountOfInstances + " instances made");
	}

	/**
	 * get a list of instances with the instanceIDs we use
	 * @return list of instances
	 */
	public List<Instance> getInstances() {
		try {
			DescribeInstancesRequest request = new DescribeInstancesRequest();
			DescribeInstancesResult ec2Response = amazonEC2Client
					.describeInstances(request
							.withInstanceIds(instanceIDsStrings));
			List<Reservation> requests = ec2Response.getReservations();
			if (requests.size() == 1) {
				return requests.get(0).getInstances();
			} else {
				System.out
						.println("can't choose between the multiple requests when gathering instances");
				for (Reservation res : requests) {
					System.out.println(res.toString());
				}
				return requests.get(0).getInstances();
			}
		} catch (AmazonServiceException aSException) {
			System.err.println(aSException.getMessage());
			aSException.printStackTrace();
		} catch (AmazonClientException aCException) {
			System.err.println(aCException.getMessage());
			aCException.printStackTrace();
		}
		return null;
	}

	/**
	 * @return the states from the instances
	 */
	public String[] getStatesFromInstances() {
		List<Instance> instances = getInstances();
		String[] instancesStatuses = new String[instances.size()];
		System.out.println("Collecting states.");
		for (int i = 0; i < instances.size(); i++) {
			instancesStatuses[i] = instances.get(i).getState().toString();
		}
		return instancesStatuses;
	}

	/**
	 * @return public dns names to connect to
	 */
	public List<String> getInstancesPublicDnsNames() {
		DescribeInstancesResult describeInstancesRequest = amazonEC2Client
				.describeInstances();
		List<Reservation> reservations = describeInstancesRequest
				.getReservations();
		int count = instanceIDsStrings.size();
		List<String> result = new ArrayList<String>();
		for (Reservation reservation : reservations) {
			if (count > 0) {
				for (Instance instance : reservation.getInstances()) {
					if (instanceIDsStrings.contains(instance.getInstanceId())) {
						count--;
						result.add(instance.getPublicDnsName());
					}
				}
			} else {
				break;
			}
		}
		return result;
	}
}
