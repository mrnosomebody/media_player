package sample;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.*;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class Controller {

    private String path;
    private MediaPlayer media_player;
    private MediaPlayer previous_media_player = null;
    private float[] correctedMagnitude;
    private final String apiKey = "AIzaSyAaGjBjnZvHxQ77zsCCqggjGOqTVIaFG1U";
    private final String cx = "dbbea6f242d95eced";
    private final String projectName = "MusicPlayer";

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
    private Label song_name;

    private AudioEqualizer eq;

    public final AudioEqualizer equalizer() {
        eq = media_player.getAudioEqualizer();

        ObservableList<EqualizerBand> band_list = eq.getBands();
        for (char n = 0; n < 10; n++) {
            EqualizerBand eq_band = band_list.get(n);
            eq_band.setGain(EqualizerBand.MAX_GAIN);
            band_list.set(n, eq_band);

        }
        return eq;
    }

    public void choose_file() throws IOException {
        if (media_player != null) {
            media_player.stop();
        }
        FileChooser file_chooser = new FileChooser();
        File file = file_chooser.showOpenDialog(null);
        path = file.toURI().toString();

        if (path != null) {
            Media media = new Media(path);
            media_player = new MediaPlayer(media);
            song_name.setText(path.split("/")[path.split("/").length - 1].replaceAll("%20", " "));

//            Image image;
//            image = new Image("");
//            album_card.setImage(image);
            series1 = new XYChart.Series<String, Number>();
            series1Data = new XYChart.Data[128];
            categories = new String[128];
            for (int i = 0; i < series1Data.length; i++) {
                categories[i] = Integer.toString(i + 1);
                series1Data[i] = new XYChart.Data<String, Number>(categories[i], 50);
                series1.getData().add(series1Data[i]);
            }
            visualizator.getData().add(series1);

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

