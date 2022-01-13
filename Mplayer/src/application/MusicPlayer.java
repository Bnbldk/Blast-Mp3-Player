package application;

/** 
Bruno Nunes Borges

MP3 MPlayr Media Player - BlastBox MP3 Player Project
*/

//import File to get music file
import java.io.File;

import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
//import ChangeListener to detect a selection change in either of ListViews
import javafx.beans.value.ChangeListener;
//import ObservableValue for event method of selection change in ListView
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
//import ObservableList and FXCollections to use ListView with getItems() method
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
//imports for components in the application
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;

// import javafx.scene.control.ProgressBar;
// import javafx.scene.control.TextField;
// import javafx.scene.control.Slider;

//imports for layout
import javafx.scene.layout.GridPane;
//import MediaPlayer to play Mp3 files
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.stage.Stage;
//import Duration to get duration status of playing music  
import javafx.util.Duration;

public class MusicPlayer extends Application { //

	// create labels
	Label lblTracks, lblPlayList, lblCurrentMusic, lblVolume, lblStatus;

	// create list views
	ListView<String> lvAvailableTracks, lvSelectedTracks;

	// create buttons
	Button btnAdd, btnRemove, btnRemoveAll, btnPlay, btnPause, btnStop;

	// create sliders
	Slider slrVolume, slrStatus;

	// create media and mediaPlayer
	MediaPlayer mediaPlayer;
	Media media;
	int playingTrack;

	/** Constructor */
	public MusicPlayer() {

		// instantiate labels
		lblTracks = new Label("Available Tracks:");
		lblPlayList = new Label("Selected Tracks:");
		lblCurrentMusic = new Label("");
		lblVolume = new Label("Volume:");
		lblStatus = new Label("Status");

		// Instantiate List views
		lvAvailableTracks = new ListView<String>();
		lvSelectedTracks = new ListView<String>();

		// Instantiate buttons:
		btnAdd = new Button("Add >");
		btnRemove = new Button("< Remove");
		btnRemoveAll = new Button("<< Remove All");
		btnPlay = new Button("Play");
		btnPause = new Button("Pause");
		btnStop = new Button("Stop");

		// Instantiate slides:
		slrVolume = new Slider();
		slrStatus = new Slider();

		// Playing track initialize
		playingTrack = -1;

	}// constructor()

	/** List all files inside a folder */
	private ObservableList<String> getFilesInFolder(String folderName) {

		// ObvservableList for the Music tracks
		ObservableList<String> musicTracks = FXCollections.observableArrayList();

		// string array to store a list of playable files
		String[] fileList;

		// Instantiate File and call list() to get a directory listing
		fileList = new File(folderName).list();

		// add the array of files to the Music tracks observable list
		musicTracks.addAll(fileList);

		// return the observable list.
		return musicTracks;

	}// getFilesInFolder()

	/** Add from lvAvailableTracks to the list of lvSelectedTracks */
	private void addMusicTrack() {

		// Get the item from lvAvailableTracks and add them to lvSelectedTracks
		lvSelectedTracks.getItems().add(lvAvailableTracks.getSelectionModel().getSelectedItem());

		// Get the index of selected item in lvAvailableTracks list view
		int selectedIdx = lvAvailableTracks.getSelectionModel().getSelectedIndex();

		// If index is not -1, that means an item is selected
		// and remove the selected item using the selection index
		if (!(selectedIdx == -1))
			lvAvailableTracks.getItems().remove(selectedIdx);

		// Enable the Remove All button when at least one track is selected
		btnRemoveAll.setDisable(false);
		btnRemoveAll.setDisable(false);

		// If size of lvAvaliableTracks equals 0, disable Add button
		if (lvAvailableTracks.getItems().size() == 0) {
			btnAdd.setDisable(true);
		}

	}// addMusicTrack()

	/** Remove from lvSelectedTracks to the list of lvAvailableTracks */
	private void removeItemFromSelectedTracks() {

		// get the item from lvSelectedTracks and add them to lvAvailableTracks
		lvAvailableTracks.getItems().add(lvSelectedTracks.getSelectionModel().getSelectedItem());

		// get the index of selected item in lvSelectedTracks list view
		int selectedIdx = lvSelectedTracks.getSelectionModel().getSelectedIndex();

		
		
		// If index is not -1, that means an item is selected
		if (!(selectedIdx == -1)) {
			// remove the selected item using the selection index
			lvSelectedTracks.getItems().remove(selectedIdx);		
			
			// if selectedIndx smaller than playingTrack, update playingTrack
			if (selectedIdx < playingTrack)
				playingTrack--;

			// if selectedIndx equal to playingTrack, stop mediaPlayer and update
			// playingTrack
			else if (selectedIdx == playingTrack) {
				mediaPlayer.stop();
				playingTrack = -1;	
				lblCurrentMusic.setText("");				
			}			
		}	

		// if no item is left in lvSelectedTracks
		// disable buttons remove, remove all and play buttons
		if (lvSelectedTracks.getItems().size() == 0) {
			btnRemoveAll.setDisable(true);
			btnPlay.setDisable(true);
		}
	}// removeItemFromSelectedTracks()

	/** Remove all items from lvSelectedTracks to the list of lvAvailableTracks */
	private void removeAllItemsFromSelectedTracks() {

		// For loop iterate through the selected track size removing
		// the first track until remove all the tracks from the selected list
		int size = lvSelectedTracks.getItems().size();
		for (int i = size; i > 0; i--) {
			// Add the first item to available tracks list view.
			lvAvailableTracks.getItems().add(lvSelectedTracks.getItems().get(0));

			// Remove the first item from selected tracks list view.
			lvSelectedTracks.getItems().remove(0);
		}

		// Removing all tracks will invoke the stop selected track and setting the current music to none
		if (mediaPlayer != null) {
			stopSelectecTrack();
			lblCurrentMusic.setText("");
		}		
		

		// Disable btnRemove, btnRemoveAll and btnPlay and enable btnAdd
		btnRemove.setDisable(true);
		btnRemoveAll.setDisable(true);
		btnPlay.setDisable(true);
		btnAdd.setDisable(false);

	}// removeAllItemsFromSelectedTracks()

	/** Play Selected Track */
	private void playSelectedTrack() {

		// get the index of selected item in lvSelectedTracks list view
		int selectedIdx = lvSelectedTracks.getSelectionModel().getSelectedIndex();

		// If index is not -1, that means an item is selected
		if (!(selectedIdx == -1)) {

			// If the media player is not null and the status is paused
			// then play the track
			// or (else)
			if (mediaPlayer != null && selectedIdx == playingTrack && mediaPlayer.getStatus() == Status.PAUSED) {
				mediaPlayer.play();
			} else {
				// If the media player is not null and the status is PLAYING then
				// stop track
				if (mediaPlayer != null && mediaPlayer.getStatus() == Status.PLAYING)
					mediaPlayer.stop();

				// Store the name of the track to play
				String trackToPlay = lvSelectedTracks.getItems().get(selectedIdx);

				// Creating the path of the track music
				String path = "./Music/" + trackToPlay;

				// Creating media file and instantiate media player
				media = new Media(new File(path).toURI().toString());
				mediaPlayer = new MediaPlayer(media);
				playingTrack = selectedIdx;

				//Show the current track music above the status
				lblCurrentMusic.setText(String.valueOf(trackToPlay));
				
				// Store the track duration and current track duration to show the per
				mediaPlayer.currentTimeProperty().addListener((InvalidationListener) ae -> {
					Duration trackDuration = mediaPlayer.getTotalDuration();
					Duration currentDuration = mediaPlayer.getCurrentTime();
					
			

					// calculate the track slider position.
					slrStatus.setValue((currentDuration.toSeconds() * 100) / trackDuration.toSeconds());

					// show the current track position in a label.
					double minutes = Math.floor(currentDuration.toMinutes());
					double seconds = Math.floor(currentDuration.toSeconds() % 60);

					int m = (int) minutes;
					int s = (int) seconds;

					// show played time on lblStatus
					lblStatus.setText("Status: Playing " + m + ":" + s);

				});

				// set the position of slider according to volume of the player
				slrVolume.setValue(mediaPlayer.getVolume() * 5);

				// add event handler to the event of changing slider
				slrVolume.valueProperty().addListener(new InvalidationListener() {

					@Override
					// set the volume of player according to value of slider
					public void invalidated(Observable observable) {
						mediaPlayer.setVolume(slrVolume.getValue() / 100);
					}
				});

				// Play music.
				mediaPlayer.play();

			}

			// Disable btnPlay.
			btnPlay.setDisable(true);

			// Enable btnPause and btnStop.
			btnPause.setDisable(false);
			btnStop.setDisable(false);
		}

	}// playSelectedTrack()

	/** Pause track */
	private void pauseSelectedTrack() {

		// pause playing if it is in play mode
		if (mediaPlayer.getStatus() == Status.PLAYING) {
			mediaPlayer.pause();

			// enable btnPlay button
			btnPlay.setDisable(false);

			// disable btnPause and btnStop
			btnPause.setDisable(true);
			btnStop.setDisable(true);
		}

	}// pauseSelectedTrack()

	/** Stop selected track */
	private void stopSelectecTrack() {

		// if mediaMplayer status is in playing mode.
		if (mediaPlayer.getStatus() == Status.PLAYING) {
			// Stop playing track.
			mediaPlayer.stop();

			// Enable btnPlay button.
			btnPlay.setDisable(false);

			// disable btnPause and btnStop
			btnPause.setDisable(true);
			btnStop.setDisable(true);
			lblCurrentMusic.setText("");
		}

	}// stopSelectecTrack()

	/** Start application */
	@Override
	public void start(Stage pStage) throws Exception {

		// set the title
		pStage.setTitle("BlastBox MP3 Player");

		// set width and height
		pStage.setWidth(750);
		pStage.setHeight(500);

		// create a grid pane
		GridPane gp = new GridPane();
		
		// set alignment baseline to center
		gp.setAlignment(Pos.BASELINE_CENTER);

		// set gap
		gp.setHgap(10);
		gp.setVgap(10);

		// set margin
		gp.setPadding(new Insets(15));

		// set labels, list views, buttons and sliders
		gp.add(lblTracks, 0, 0);
		gp.add(lvAvailableTracks, 0, 1, 2, 12);
		
		gp.add(btnAdd, 3, 4);
		gp.add(btnRemove, 3, 5);
		gp.add(btnRemoveAll, 3, 6);
		gp.add(btnPlay, 3, 7);
		gp.add(btnPause, 3, 8);
		gp.add(btnStop, 3, 9);
		gp.add(lblVolume, 3, 10);
		gp.add(slrVolume, 3, 11);
		
		gp.add(lblPlayList, 4, 0);
		gp.add(lvSelectedTracks, 4, 1, 2, 12);
		
		gp.add(lblCurrentMusic, 4, 13);
		
		gp.add(lblStatus, 4, 14);
		gp.add(slrStatus, 4, 15);

		// Create a scene
		Scene s = new Scene(gp);

		// CSS Style to the scene layout
//		s.getStylesheets().add("style.css");

		// Set application image on the scene
//		pStage.getIcons().add(new Image("iconpod.png"));

		// Set the scene
		pStage.setScene(s);

		// Show the stage
		pStage.show();

	}// start()

	@Override /** Events Handling */
	public void init() {

		// disable button by default
		btnAdd.setDisable(true);

		// handle event when clicking on button
		btnAdd.setOnAction(ae -> addMusicTrack());

		// event handler for selecting an item in lvAvailableTracks
		lvAvailableTracks.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			// event handler function for selection change
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

				// enable btnAdd when an item is selected in lvAvailableTracks
				btnAdd.setDisable(false);
			}
		});

		// disable button by default
		btnRemove.setDisable(true);

		// handle event when clicking on button
		btnRemove.setOnAction(ae -> removeItemFromSelectedTracks());

		// disable button by default
		btnRemoveAll.setDisable(true);

		// handle event when clicking on button
		btnRemoveAll.setOnAction(ae -> removeAllItemsFromSelectedTracks());

		// handle event when clicking on button
		btnPlay.setOnAction(ae -> playSelectedTrack());

		// disable button by default
		btnPlay.setDisable(true);

		// handle event when clicking on button
		btnPause.setOnAction(ae -> pauseSelectedTrack());

		// disable button by default
		btnPause.setDisable(true);

		// handle event when clicking on button
		btnStop.setOnAction(ae -> stopSelectecTrack());

		// disable button by default
		btnStop.setDisable(true);

		// adding event handler for selecting an item in lvAvailableTracks
		lvSelectedTracks.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

			// event handler function for selection change in selected tracks list view
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

				// enable btnRemove, btnRemoveAll and btnPlay when an item is selected in
				// lvSelectedTracks list view
				btnRemove.setDisable(false);
				btnRemoveAll.setDisable(false);
				btnPlay.setDisable(false);
			}
		});

		// manage listViews width and height
		lvAvailableTracks.setMinWidth(50);
		lvAvailableTracks.setMinHeight(100);
		lvSelectedTracks.setMinWidth(50);
		lvSelectedTracks.setMinHeight(100);

		// manage buttons minimum width
		int btnMinWidth = 115;
		btnAdd.setMinWidth(btnMinWidth);
		btnRemove.setMinWidth(btnMinWidth);
		btnRemoveAll.setMinWidth(btnMinWidth);
		btnPlay.setMinWidth(btnMinWidth);
		btnPause.setMinWidth(btnMinWidth);
		btnStop.setMinWidth(btnMinWidth);

		// add .mp3 files into available tracks listView
		lvAvailableTracks.setItems(getFilesInFolder("./Music/"));

	}// init()

	public static void main(String[] args) {
		launch(args);
	}// main()

}// class