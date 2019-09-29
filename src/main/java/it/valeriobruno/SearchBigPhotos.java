package it.valeriobruno;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.File.ImageMediaMetadata;
import com.google.api.services.drive.model.FileList;

public class SearchBigPhotos {

	public static final int PAGE_SIZE = 100;

	private final ReviewRepo reviewRepo;

	private final Drive driveService;

	private final QueryCheckPointImpl queryCheckPoint;

	public SearchBigPhotos(ReviewRepo reviewRepo, QueryCheckPointImpl queryCheckPoint, Drive driveService) {
		this.reviewRepo = reviewRepo;
		this.queryCheckPoint = queryCheckPoint;
		this.driveService = driveService;
	}

	public void startSearching() {
		while (!reviewRepo.hasEnoughToReview()) {
			Drive.Files.List listQuery;

			queryCheckPoint.loadPointer();
			try {
				
				System.out.println("querying Google Drive... ");
				listQuery = driveService.files().list().setPageSize(PAGE_SIZE)
						.setFields("nextPageToken, files(id, name, mimeType, imageMediaMetadata)")
						.setOrderBy("createdTime asc")
						.setQ("mimeType contains 'image'") // image/jpeg image/png etc..
						.setPageToken(queryCheckPoint.getStringPointer());
				Date beforeQuery = new Date();
				FileList result = listQuery.execute();

				List<File> files = result.getFiles();

				String page = result.getNextPageToken();
				queryCheckPoint.setStringPointer(page);
				System.out.println("Reached checkpoint: "+page);
				Date afterQuery = new Date();
				long elapsed = afterQuery.getTime() - beforeQuery.getTime();
				System.out.println("Query in (seconds) "+elapsed/1000);
				files.stream().filter( file -> getImageResolution(file) >= 16000000L).forEach(file -> {
					try {
						
						reviewRepo.download(file);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				});;
				

			} catch (IOException e) {
				e.printStackTrace();
				return;
			}

			queryCheckPoint.savePointer();

		}
		System.out.println("Enough pictures to review. Or no-more pictures to download.");

	}

	private long getImageResolution(File file)
	{
		long res = 0l;
		ImageMediaMetadata metadata = file.getImageMediaMetadata();
		if(metadata != null
								&& metadata.getHeight() != null
								&& metadata.getWidth() != null)
		{
			res = ((long) metadata.getHeight()) * ((long) metadata.getWidth());
		}
		
		return res;
	}
	
	public static void main(String[] args) throws GeneralSecurityException, IOException {

		final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        final Credential credentials = getCredentials(HTTP_TRANSPORT);
		Drive driveService = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credentials)
        		.setHttpRequestInitializer(new HttpRequestInitializer() {
					
					@Override
					public void initialize(HttpRequest httpRequest) throws IOException {
						credentials.initialize(httpRequest);
		                httpRequest.setConnectTimeout(300 * 60000);  // 300 minutes connect timeout
		                httpRequest.setReadTimeout(300 * 60000);
						
					}
				})
                .setApplicationName(APPLICATION_NAME)
                .build();
        
		ReviewRepoImpl reviewRepo = new ReviewRepoImpl(new java.io.File("./review"),driveService);
		SearchBigPhotos sbp = new SearchBigPhotos(reviewRepo, new QueryCheckPointImpl(),driveService);

		sbp.startSearching();
	}
	
    
    private static final String APPLICATION_NAME = SearchBigPhotos.class.getSimpleName();
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = SearchBigPhotos.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
    
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE_METADATA_READONLY,DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    
}
