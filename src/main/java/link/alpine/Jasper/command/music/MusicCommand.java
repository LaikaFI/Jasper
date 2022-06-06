package link.alpine.Jasper.command.music;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import link.alpine.Jasper.command.CommandClass;
import link.alpine.Jasper.model.audio.AudioInfo;
import link.alpine.Jasper.model.audio.AudioPlayerSendHandler;
import link.alpine.Jasper.model.audio.TrackManager;
import link.alpine.Jasper.util.CommandInfo;
import link.alpine.Jasper.util.CommandType;
import link.alpine.Jasper.util.EmbedUI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static link.alpine.Jasper.util.LoggingManager.slashLog;

/**
 * Music Command
 * Most code taken from SHIRO Project
 * @author Laika
 */
public class MusicCommand extends CommandClass {

    private static final int PLAYLIST_LIMIT = 200;
    private static final AudioPlayerManager myManager = new DefaultAudioPlayerManager();
    private static final Map<String, Map.Entry<AudioPlayer, TrackManager>> players = new HashMap<>();

    private static final String CD = "\uD83D\uDCBF";
    private static final String DVD = "\uD83D\uDCC0";
    private static final String MIC = "\uD83C\uDFA4 **|>** ";

    private static final String QUEUE_TITLE = "__%s has added %d new track%s to the Queue:__";
    private static final String QUEUE_DESCRIPTION = "%s **|>**  %s\n%s\n%s %s\n%s";
    private static final String QUEUE_INFO = "Info about the Queue: (Size - %d)";
    private static final String ERROR = "Error while loading \"%s\"";

    private boolean enabled = true;

    public MusicCommand() {
        AudioSourceManagers.registerRemoteSources(myManager);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getName() {
        return "Music";
    }

    @Override
    public void newCommand(String name, SlashCommandInteractionEvent e) {
        if(e.getGuild() != null) {
            switch (name) {
                case "nowplaying":
                    slashLog(e);
                    e.deferReply().queue();
                    if (!hasPlayer(e.getGuild()) || getPlayer(e.getGuild()).getPlayingTrack() == null) { // No song is playing
                        e.getHook().sendMessage("No song is playing.").queue();
                    } else {
                        var track = getPlayer(e.getGuild()).getPlayingTrack();
                        //Works
                        var eb = new EmbedBuilder();
                        eb.setColor(EmbedUI.SUCCESS);
                        eb.setTitle("Track Info");
                        eb.setDescription("Currently Playing - " + track.getInfo().title);
                        eb.addField("Time", "["+ getTimestamp(track.getPosition()) + "/" + getTimestamp(track.getInfo().length) + "]", false);
                        eb.addField("Creator", track.getInfo().author, false);
                        eb.addField("Queued By", getTrackManager(e.getGuild()).getTrackInfo(track).getAuthor().getUser().getName(), false);
                        e.getHook().sendMessageEmbeds(eb.build()).queue();
                    }
                    break;
                case "queue":
                    slashLog(e);
                    e.deferReply().queue();
                    if (!hasPlayer(e.getGuild()) || getTrackManager(e.getGuild()).getQueuedTracks().isEmpty()) {
                        e.getHook().sendMessage("The queue is empty.").queue();
                    } else {
                        var sb = new StringBuilder();
                        Set<AudioInfo> queue = getTrackManager(e.getGuild()).getQueuedTracks();
                        queue.forEach(audioInfo -> sb.append(buildQueueMessage(audioInfo)));
                        var embedTitle = String.format(QUEUE_INFO, queue.size());

                        if (sb.length() <= 1960) {
                            var eb = new EmbedBuilder();
                            eb.setColor(EmbedUI.SUCCESS);
                            eb.setTitle(embedTitle);
                            eb.addField("In Queue", "**>** " + sb.toString(), false);
                            e.getHook().sendMessageEmbeds(eb.build()).queue();
                        } else {
                            var qFile = new File("queue.txt");
                            try {
                                FileUtils.write(qFile, sb.toString(), "UTF-8", false);
                                e.getHook().sendMessage("**Queue was too large to put into text, linked file below contains all songs queued.").queue();
                                e.getHook().sendFile(qFile, qFile.getName(), null).queue();
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                            if (!qFile.delete()) { // Delete the queue file after we're done
                                qFile.deleteOnExit();
                            }
                        }
                    }
                    break;
                case "skip":
                    slashLog(e);
                    e.deferReply().queue();
                    if (isIdle(e.getHook(), e.getGuild())) return;

                    if (isCurrentDj(e.getMember())) {
                        forceSkipTrack(e.getGuild(), e.getHook());
                    } else {
                        AudioInfo info = getTrackManager(e.getGuild()).getTrackInfo(getPlayer(e.getGuild()).getPlayingTrack());
                        if (info.hasVoted(e.getUser())) {
                            e.getHook().sendMessage("\u26A0 You've already voted to skip this song!").queue();
                        } else {
                            // Determine requisite votes.
                            int votes = info.getSkips();
                            int channelUsers = info.getAuthor().getVoiceState().getChannel().getMembers().size()-1;
                            int required = channelUsers/2;
                            if (votes+1 >= required){
                                getPlayer(e.getGuild()).stopTrack();
                                e.getHook().sendMessage("\u23E9 Skipping current track.").queue();
                            } else {
                                info.addSkip(e.getUser());
                                e.getHook().sendMessage("**" + e.getUser().getName() + "** voted to skip the track. [" + (votes+1) + "/" + (required) + "]").queue();
                            }
                        }
                    }
                    break;
                case "forceskip":
                    slashLog(e);
                    e.deferReply().queue();
                    if (isIdle(e.getHook(), e.getGuild())) return;
                    if (isCurrentDj(e.getMember()) || isDj(e.getMember())) {
                        forceSkipTrack(e.getGuild(), e.getHook());
                    } else {
                        e.getHook().sendMessage("You don't have permission to do that!\n"
                                + "Use **/skip** to cast a vote!").queue();
                    }
                    break;
                case "reset":
                    slashLog(e);
                    e.deferReply().queue();
                    if (!e.getMember().getPermissions().contains(Permission.ADMINISTRATOR) && !e.getMember().isOwner()) {
                        e.getHook().sendMessage("You don't have the required permissions to do that! [ADMIN]").queue();
                    } else {
                        reset(e.getGuild());
                        e.getHook().sendMessage("\uD83D\uDD04 Resetting the music player..").queue();
                    }
                    break;
                case "shuffle":
                    slashLog(e);
                    e.deferReply().queue();
                    if (isIdle(e.getHook(), e.getGuild())) return;

                    if (isDj(e.getMember())) {
                        getTrackManager(e.getGuild()).shuffleQueue();
                        e.getHook().sendMessage("\u2705 Shuffled the queue!").queue();
                    } else {
                        e.getHook().sendMessage("\u26D4 You don't have the permission to do that!").queue();
                    }
                    break;
                case "stop":
                    e.deferReply().queue();
                    if(isIdle(e.getHook(), e.getGuild())) return;

                    getTrackManager(e.getGuild()).purgeQueue();
                    getPlayer(e.getGuild()).stopTrack();
                    e.getHook().sendMessage("Stopped the track. :boom:").queue();
                    break;
                case "play":
                    e.deferReply().queue();
                    var input = e.getOption("url").getAsString();
                    slashLog(e, "with search \"" + input + "\".");
                    if(input.contains("https://")) {
                        loadTrack(input, e.getMember(), e.getHook());
                    } else {
                        loadTrack("ytsearch: " + input, e.getMember(), e.getHook());
                    }
                    break;
            }
        }
    }

    private boolean hasPlayer(Guild guild) {
        return players.containsKey(guild.getId());
    }

    @Override
    public List<CommandInfo> getSlashCommandInfo() {
        List<CommandInfo> cil = new ArrayList<>();
        CommandInfo ci = new CommandInfo("nowplaying", "Displays the song being currently played.", CommandType.COMMAND);
        CommandInfo ci1 = new CommandInfo("queue", "Displays the songs currently queued.", CommandType.COMMAND);
        CommandInfo ci2 = new CommandInfo("skip", "Votes to skip the song currently playing. Or just skips it if you're the DJ.", CommandType.COMMAND);
        CommandInfo ci3 = new CommandInfo("forceskip", "Forcibly skips the song that is currently playing.", CommandType.COMMAND);
        CommandInfo ci4 = new CommandInfo("reset", "Resets the song player. (Admin Only)", CommandType.COMMAND);
        CommandInfo ci5 = new CommandInfo("play", "Plays a new song from a URL", CommandType.COMMAND);
        ci5.addOption("url", "The URL or title of the song that you wanted to play.", OptionType.STRING, true);
        cil.add(ci);
        cil.add(ci1);
        cil.add(ci2);
        cil.add(ci3);
        cil.add(ci4);
        cil.add(ci5);
        return cil;
    }

    private AudioPlayer getPlayer(Guild guild) {
        AudioPlayer p;
        if (hasPlayer(guild)) {
            p = players.get(guild.getId()).getKey();
        } else {
            p = createPlayer(guild);
        }
        return p;
    }

    private TrackManager getTrackManager(Guild guild) {
        return players.get(guild.getId()).getValue();
    }

    private AudioPlayer createPlayer(Guild guild) {
        var nPlayer = myManager.createPlayer();
        var manager = new TrackManager(nPlayer);
        nPlayer.addListener(manager);
        guild.getAudioManager().setSendingHandler(new AudioPlayerSendHandler(nPlayer));
        players.put(guild.getId(), new AbstractMap.SimpleEntry<>(nPlayer, manager));
        return nPlayer;
    }

    private void reset(Guild guild) {
        players.remove(guild.getId());
        getPlayer(guild).destroy();
        getTrackManager(guild).purgeQueue();
        guild.getAudioManager().closeAudioConnection();
    }

    private void loadTrack(String identifier, Member author, InteractionHook chat) {
        if (author.getVoiceState().getChannel() == null) {
            chat.sendMessage("You are not in a Voice Channel!").queue();
            return;
        }

        Guild guild = author.getGuild();
        getPlayer(guild); // Make sure this guild has a player.

        myManager.loadItemOrdered(guild, identifier, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                var eb = new EmbedBuilder();
                eb.setColor(EmbedUI.SUCCESS);
                eb.setTitle(author.getEffectiveName() + " has loaded " + track.getInfo().title);
                eb.addField("Track Info", "Creator: " + track.getInfo().author + "\nLength: " + getTimestamp(track.getInfo().length), false);
                chat.sendMessageEmbeds(eb.build()).queue();
                getTrackManager(guild).queue(track, author);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (playlist.getSelectedTrack() != null) {
                    trackLoaded(playlist.getSelectedTrack());
                } else if (playlist.isSearchResult()) {
                    trackLoaded(playlist.getTracks().get(0));
                } else {
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setColor(EmbedUI.SUCCESS);
                    eb.setTitle(author.getEffectiveName() + " has loaded " + playlist.getTracks().size() + " songs into the queue");
                    chat.sendMessageEmbeds(eb.build()).queue();
                    for (int i = 0; i < Math.min(playlist.getTracks().size(), PLAYLIST_LIMIT); i++) {
                        getTrackManager(guild).queue(playlist.getTracks().get(i), author);
                    }
                }
            }

            @Override
            public void noMatches() {
                chat.sendMessage("\u26A0 No playable tracks were found. (If searching, try the command 'ytplay' instead!)").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                chat.sendMessage("\u26D4 " + exception.getLocalizedMessage()).queue();
            }
        });
        //tryToDelete(msg);
    }

    private boolean isDj(Member member) {
        return member.getRoles().stream().anyMatch(r -> r.getName().equals("DJ"));
    }

    private boolean isCurrentDj(Member member) {
        return getTrackManager(member.getGuild()).getTrackInfo(getPlayer(member.getGuild()).getPlayingTrack()).getAuthor().equals(member);
    }

    private boolean isIdle(InteractionHook chat, Guild guild) {
        if (!hasPlayer(guild) || getPlayer(guild).getPlayingTrack() == null) {
            chat.sendMessage("No music is being played at the moment!").queue();
            return true;
        }
        return false;
    }

    private void forceSkipTrack(Guild guild, InteractionHook chat) {
        getPlayer(guild).stopTrack();
        chat.sendMessage("\u23E9 Skipping track!").queue();
    }

    private String buildQueueMessage(AudioInfo info) {
        var trackInfo = info.getTrack().getInfo();
        var title = trackInfo.title;
        long length = trackInfo.length;
        return "`[ " + getTimestamp(length) + " ]` " + title + "\n";
    }

    private String getTimestamp(long milis) {
        long seconds = milis / 1000;
        long hours = Math.floorDiv(seconds, 3600);
        seconds = seconds - (hours * 3600);
        long mins = Math.floorDiv(seconds, 60);
        seconds = seconds - (mins * 60);
        return (hours == 0 ? "" : hours + ":") + String.format("%02d", mins) + ":" + String.format("%02d", seconds);
    }

    private String getOrNull(String s) {
        return s.isEmpty() ? "N/A" : s;
    }
}
