package link.alpine.Jasper.model.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.entities.AudioChannel;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Import from SHIRO project.
 * @author Jasper
 */
public class TrackManager extends AudioEventAdapter {

    private final AudioPlayer player;
    private final Queue<AudioInfo> queue;

    public TrackManager(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Queues a new track to be played.
     * @param track - the track to be played
     * @param author - the person who queued the track
     */
    public void queue(AudioTrack track, Member author) {
        AudioInfo info = new AudioInfo(track, author);
        queue.add(info);

        if (player.getPlayingTrack() == null) {
            player.playTrack(track);
        }
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        AudioInfo info = queue.element();
        AudioChannel vChan = info.getAuthor().getVoiceState().getChannel();
        if (vChan == null) { // User has left all voice channels
            player.stopTrack();
        } else {
            info.getAuthor().getGuild().getAudioManager().openAudioConnection(vChan);
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        Guild g = queue.poll().getAuthor().getGuild();
        if (queue.isEmpty()) {
            g.getAudioManager().closeAudioConnection();
        } else {
            player.playTrack(queue.element().getTrack());
        }
    }

    public void shuffleQueue() {
        List<AudioInfo> tQueue = new ArrayList<>(getQueuedTracks());
        AudioInfo current = tQueue.get(0);
        tQueue.remove(0);
        Collections.shuffle(tQueue);
        tQueue.add(0, current);
        purgeQueue();
        queue.addAll(tQueue);
    }

    public Set<AudioInfo> getQueuedTracks() {
        return new LinkedHashSet<>(queue);
    }

    public void purgeQueue() {
        queue.clear();
    }

    public void remove(AudioInfo entry) {
        queue.remove(entry);
    }

    public AudioInfo getTrackInfo(AudioTrack track) {
        return queue.stream().filter(audioInfo -> audioInfo.getTrack().equals(track)).findFirst().orElse(null);
    }
}
