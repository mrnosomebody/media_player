package sample;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.umass.lastfm.Caller;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.media.*;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.Duration;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class Controller {

    private String path;
    private MediaPlayer media_player;
    private MediaPlayer previous_media_player = null;

    private final String apiKey = "81133b7adcb966c38b5f3bee1065d54e";
    private final String cx = "dd3f703ed128e1ba0e822fc0b0592de5";
    private final String projectName = "music_player";
    private final String user = "Mrnosomebody";
    private  String album = null;
    private  String artist = null;
    private String url_to_image;

    @FXML
    private ImageView album_card;

    @FXML
    private BarChart visualizator;
    private XYChart.Data[] series1Data;
    XYChart.Series<String, Number> series1;
    String[] categories;

    @FXML
    private Slider progress_bar;

    @FXML
    private Slider volume_bar;
    
    @FXML
    private ListView<File> songs_list;

    @FXML
    private VBox vBox;

    @FXML
    private Label song_name;

    @FXML
    private Slider eq_slider;

    @FXML
    private AnchorPane anc_eq_slider;

    private AudioEqualizer eq;


    public final AudioEqualizer equalizer() {
        anc_eq_slider.setVisible(true);
        eq = media_player.getAudioEqualizer();
        ObservableList<EqualizerBand> band_list = eq.getBands();
        //equalizer slider
        eq_slider.setValue(media_player.getAudioSpectrumNumBands());
        eq_slider.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                for (int n = 0; n < 10; n++) {
                    EqualizerBand eq_band = band_list.get(n);
                    eq_band.setGain(eq_slider.getValue());
                    band_list.set(n, eq_band);
                }
            }
        });
        return eq;
    }

    public void add(){
        if (media_player != null) {
            media_player.stop();
        }
        if (media_player==null){
            //visualizator
            series1 = new XYChart.Series<String, Number>();
            series1Data = new XYChart.Data[128];
            categories = new String[128];
            for (int i = 0; i < series1Data.length; i++) {
                categories[i] = Integer.toString(i + 1);
                series1Data[i] = new XYChart.Data<String, Number>(categories[i], 50);
                series1.getData().add(series1Data[i]);
            }
            visualizator.getData().add(series1);
        }
        ObservableList<File> list = FXCollections.observableArrayList();
        FileChooser fileChooser = new FileChooser();
        list.addAll( fileChooser.showOpenMultipleDialog(null));
        songs_list.setItems(list);
        vBox.getChildren().add(songs_list);
        path = list.get(0).toURI().toString();

        Media media = new Media(path);
        media_player = new MediaPlayer(media);
        song_name.setText(path.split("/")[path.split("/").length - 1].replaceAll("%20", " "));

        //Making the slider coming to the end only when it is actually the end
        media_player.setOnReady(new Runnable() {
            @Override
            public void run() {
                Duration total = media_player.getTotalDuration();
                progress_bar.setMax(total.toSeconds());
            }
        });
    }

    public void click(){
        if (previous_media_player != null) {
            media_player.stop();
            previous_media_player = media_player;
        }
        //volume_bar.setValue(media_player.getVolume());
        path = songs_list.getSelectionModel().getSelectedItem().toURI().toString();

        if (path != null) {
            Media media = new Media(path);
            media_player = new MediaPlayer(media);
            song_name.setText(path.split("/")[path.split("/").length - 1].replaceAll("%20", " "));

            //Making the slider coming to the end only when it is actually the end
            media_player.setOnReady(new Runnable() {
                @Override
                public void run() {
                    Duration total = media_player.getTotalDuration();
                    progress_bar.setMax(total.toSeconds());
                }
            });
        }
        //Volume slider
        volume_bar.setValue(media_player.getVolume() * 100);
        volume_bar.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                media_player.setVolume(volume_bar.getValue() / 100);
            }
        });

        //img find
        String song_full_name=path.split("/")[path.split("/").length - 1].replaceAll("%20", " ");
        artist = song_full_name.split(" -")[0].replaceAll(" ","%20");
        album = song_full_name.split("-")[1].split("\\.")[0].replaceFirst(" ","").replaceAll(" ","%20");
        String query = "https://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=81133b7adcb966c38b5f3bee1065d54e&artist="+artist+"&album="+album+"&format=json";

        HttpURLConnection connection = null;
        try{
            connection = (HttpURLConnection) new URL(query).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            StringBuilder sb = new StringBuilder();

            if (HttpURLConnection.HTTP_OK==connection.getResponseCode()){
                BufferedReader in = new BufferedReader((new InputStreamReader(connection.getInputStream())));
                String line;
                while ((line=in.readLine())!=null){
                    sb.append(line);
                    sb.append("\n");
                }

                if (!sb.toString().split(",")[0].equals("{\"error\":6")) {
                    JsonObject jsonObject = new JsonParser().parse(sb.toString()).getAsJsonObject();
                    JsonElement temp = jsonObject.getAsJsonObject("album").getAsJsonArray("image").get(3).getAsJsonObject().get("#text");
                    url_to_image = temp.toString().replaceAll("\"", "");
                    if (url_to_image.length() > 3) {
                        Image image = new Image(url_to_image);
                        album_card.setImage(image);
                    }
                    else {
                        Image image = new Image("images/media.png");
                        album_card.setImage(image);
                    }
                }else {
                    System.out.println("Album not found");
                }


            }else{
                System.out.println("Error "+connection.getResponseCode()+","+connection.getResponseMessage());
            }
        }catch (Throwable cause){
            cause.printStackTrace();
        }finally {
            if (connection!= null){
                connection.disconnect();
            }
        }
    }

    public void choose_file() throws IOException {
        if (media_player != null) {
            media_player.stop();
        }
        if (media_player==null){
            //visualizator
            series1 = new XYChart.Series<String, Number>();
            series1Data = new XYChart.Data[128];
            categories = new String[128];
            for (int i = 0; i < series1Data.length; i++) {
                categories[i] = Integer.toString(i + 1);
                series1Data[i] = new XYChart.Data<String, Number>(categories[i], 50);
                series1.getData().add(series1Data[i]);
            }
            visualizator.getData().add(series1);
        }

        FileChooser file_chooser = new FileChooser();
        File file = file_chooser.showOpenDialog(null);
        path = file.toURI().toString();


        if (path != null) {
            Media media = new Media(path);
            media_player = new MediaPlayer(media);
            song_name.setText(path.split("/")[path.split("/").length - 1].replaceAll("%20", " "));

            //Making the slider coming to the end only when it is actually the end
            media_player.setOnReady(new Runnable() {
                @Override
                public void run() {
                    Duration total = media_player.getTotalDuration();
                    progress_bar.setMax(total.toSeconds());
                }
            });
        }
        //Volume slider
        volume_bar.setValue(media_player.getVolume() * 100);
        volume_bar.valueProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                media_player.setVolume(volume_bar.getValue() / 100);
            }
        });
        //img find
        String song_full_name=path.split("/")[path.split("/").length - 1].replaceAll("%20", " ");
        artist = song_full_name.split(" -")[0].replaceAll(" ","%20");
        album = song_full_name.split("-")[1].split("\\.")[0].replaceFirst(" ","").replaceAll(" ","%20");
        String query = "https://ws.audioscrobbler.com/2.0/?method=album.getinfo&api_key=81133b7adcb966c38b5f3bee1065d54e&artist="+artist+"&album="+album+"&format=json";

        HttpURLConnection connection = null;
        try{
            connection = (HttpURLConnection) new URL(query).openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.connect();
            StringBuilder sb = new StringBuilder();

            if (HttpURLConnection.HTTP_OK==connection.getResponseCode()){
                BufferedReader in = new BufferedReader((new InputStreamReader(connection.getInputStream())));
                String line;
                while ((line=in.readLine())!=null){
                    sb.append(line);
                    sb.append("\n");
                }

                if (!sb.toString().split(",")[0].equals("{\"error\":6")) {
                    JsonObject jsonObject = new JsonParser().parse(sb.toString()).getAsJsonObject();
                    JsonElement temp = jsonObject.getAsJsonObject("album").getAsJsonArray("image").get(3).getAsJsonObject().get("#text");
                    url_to_image = temp.toString().replaceAll("\"", "");
                    if (url_to_image.length() > 3) {
                        Image image = new Image(url_to_image);
                        album_card.setImage(image);
                    }
                    else {
                        Image image = new Image("images/media.png");
                        album_card.setImage(image);
                    }
                }else {
                    System.out.println("Album not found");
                }


            }else{
                System.out.println("Error "+connection.getResponseCode()+","+connection.getResponseMessage());
            }
        }catch (Throwable cause){
            cause.printStackTrace();
        }finally {
            if (connection!= null){
                connection.disconnect();
            }
        }
    }

    public void play() {
        if (previous_media_player != null) {
            media_player.stop();
            previous_media_player = media_player;
        }
        media_player.play();

        //ebanyi visualizator
        media_player.setAudioSpectrumListener(new AudioSpectrumListener() {
            public void spectrumDataUpdate(double timestamp, double duration, float[] magnitudes, float[] phases) {
                for (int i = 0; i < series1Data.length; i++) {
                    series1Data[i].setYValue(magnitudes[i] + 60);
                }
            }
        });


        //Jump to the moment where the mouse was pressed on the slider
        progress_bar.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                media_player.seek(Duration.seconds(progress_bar.getValue()));
            }
        });
        //Jump to the moment where the mouse was dragged on the slider
        progress_bar.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                media_player.seek(Duration.seconds(progress_bar.getValue()));
            }
        });
        //Slider connection with media time
        media_player.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observableValue, Duration oldValue, Duration newValue) {
                progress_bar.setValue(media_player.getCurrentTime().toSeconds());
            }
        });

    }

    public void pause() {
        media_player.pause();
    }

    public void switch_volume() {
        if (media_player.getVolume() > 0) {
            media_player.setVolume(0);
            volume_bar.setValue(0);
        } else {
            media_player.setVolume(1);
            volume_bar.setValue(100);
        }
    }


}

