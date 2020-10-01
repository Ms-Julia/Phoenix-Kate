package ealvatag.tag;

import org.jetbrains.annotations.NotNull;

/**
 * This is an enumeration of all possible fields. Some tag types do not support all these fields
 * <p>
 * This enumeration is used by subclasses to map from the common key to their implementation key
 */
@SuppressWarnings("SpellCheckingInspection")
public enum FieldKey {
    ACOUSTID_FINGERPRINT("Acoustid Fingerprint"),
    ACOUSTID_ID("Acoustid"),
    ALBUM("Album"),
    ALBUM_ARTIST("Album Artist"),
    ALBUM_ARTIST_SORT("Album Artist Sort"),
    ALBUM_ARTISTS("Album Artists"),
    ALBUM_ARTISTS_SORT("Album Artists Sort"),
    ALBUM_SORT("Album Sort"),
    AMAZON_ID("Amazon ID"),
    ARRANGER("Arranger"),
    ARRANGER_SORT("Arranger Sort"),
    ARTIST("Artist"),
    ARTISTS("Artists"),
    ARTISTS_SORT("Artists Sort"),
    ARTIST_SORT("Artist Sort"),
    BARCODE("Barcode"),
    BPM("BPM"),
    CATALOG_NO("Catalog#"),
    CLASSICAL_CATALOG("Classical Catalog"),
    CLASSICAL_NICKNAME("Classical Nickname"),
    CHOIR("Choir"),
    CHOIR_SORT("Choir Sort"),
    COMMENT("Comment"),
    COMPOSER("Composer"),
    COMPOSER_SORT("Composer Sort"),
    CONDUCTOR("Conductor"),
    CONDUCTOR_SORT("Conductor Sort"),
    COUNTRY("Country"),
    COVER_ART("Cover Art"),
    CUSTOM1("Custom 1"),
    CUSTOM2("Custom 2"),
    CUSTOM3("Custom 3"),
    CUSTOM4("Custom 4"),
    CUSTOM5("Custom 5"),
    DISC_NO("Disc#"),
    DISC_SUBTITLE("Disc Subtitle"),
    DISC_TOTAL("Disc Total"),
    DJMIXER("DJ Mixer"),
    ENCODER("Encoder"),
    ENGINEER("Engineer"),
    ENSEMBLE("Ensemble"),
    ENSEMBLE_SORT("Ensemble Sort"),
    FBPM("FBPM"),
    GENRE("Genre"),
    GROUPING("Grouping"),
    INVOLVED_PERSON("Involved Person"),
    ISRC("ISRC"),
    IS_CLASSICAL("Is Classical"),
    IS_SOUNDTRACK("Is Soundtrack"),
    IS_COMPILATION("Is Compilation"),
    ITUNES_GROUPING("iTunes Grouping"),
    KEY("Key"),
    LANGUAGE("Language"),
    LYRICIST("Lyricist"),
    LYRICS("Lyrics"),
    MEDIA("Media"),
    MIXER("Mixer"),
    MOOD("Mood"),
    MOOD_ACOUSTIC("Mood Acoustic"),
    MOOD_AGGRESSIVE("Mood Aggressive"),
    MOOD_AROUSAL("Mood Arousal"),
    MOOD_DANCEABILITY("Mood Danceability"),
    MOOD_ELECTRONIC("Mood Electronic"),
    MOOD_HAPPY("Mood Happy"),
    MOOD_INSTRUMENTAL("Mood Instrumental"),
    MOOD_PARTY("Mood Party"),
    MOOD_RELAXED("Mood Relaxed"),
    MOOD_SAD("Mood Sad"),
    MOOD_VALENCE("Mood Valence"),
    MOVEMENT("Movement"),
    MOVEMENT_NO("Movement#"),
    MOVEMENT_TOTAL("Movement Total"),
    MUSICBRAINZ_ARTISTID("Musicbrainz Artist ID"),
    MUSICBRAINZ_DISC_ID("Musicbrainz Disc ID"),
    MUSICBRAINZ_ORIGINAL_RELEASE_ID("Musicbrainz Original Release ID"),
    MUSICBRAINZ_RELEASEARTISTID("MusicbrainzReleaseartist ID"),
    MUSICBRAINZ_RELEASEID("Musicbrainz Release ID"),
    MUSICBRAINZ_RELEASE_COUNTRY("Musicbrainz Release Country"),
    MUSICBRAINZ_RELEASE_GROUP_ID("Musicbrainz Release Group ID"),
    MUSICBRAINZ_RELEASE_STATUS("Musicbrainz Release Status"),
    MUSICBRAINZ_RELEASE_TRACK_ID("Musicbrainz Release Track ID"),
    MUSICBRAINZ_RELEASE_TYPE("Musicbrainz Release Type"),
    MUSICBRAINZ_TRACK_ID("Musicbrainz Track ID"),
    MUSICBRAINZ_WORK("Musicbrainz Work"),
    MUSICBRAINZ_WORK_ID("Musicbrainz Work ID"),
    MUSICBRAINZ_WORK_COMPOSITION("Musicbrainz Work Composition"),
    MUSICBRAINZ_WORK_COMPOSITION_ID("Musicbrainz Work Composition ID"),
    MUSICBRAINZ_WORK_PART_LEVEL1("Musicbrainz Work Part Level 1"),
    MUSICBRAINZ_WORK_PART_LEVEL1_ID("Musicbrainz Work Part Level 1 ID"),
    MUSICBRAINZ_WORK_PART_LEVEL1_TYPE("Musicbrainz Work Part Level 1 Type"),
    MUSICBRAINZ_WORK_PART_LEVEL2("Musicbrainz Work Part Level 2"),
    MUSICBRAINZ_WORK_PART_LEVEL2_ID("Musicbrainz Work Part Level 2 ID"),
    MUSICBRAINZ_WORK_PART_LEVEL2_TYPE("Musicbrainz Work Part Level 2 Type"),
    MUSICBRAINZ_WORK_PART_LEVEL3("Musicbrainz Work Part Level 3"),
    MUSICBRAINZ_WORK_PART_LEVEL3_ID("Musicbrainz Work Part Level 3 ID"),
    MUSICBRAINZ_WORK_PART_LEVEL3_TYPE("Musicbrainz Work Part Level 3 Type"),
    MUSICBRAINZ_WORK_PART_LEVEL4("Musicbrainz Work Part Level 4"),
    MUSICBRAINZ_WORK_PART_LEVEL4_ID("Musicbrainz Work Part Level 4 ID"),
    MUSICBRAINZ_WORK_PART_LEVEL4_TYPE("Musicbrainz Work Part Level 4 Type"),
    MUSICBRAINZ_WORK_PART_LEVEL5("Musicbrainz Work Part Level 5"),
    MUSICBRAINZ_WORK_PART_LEVEL5_ID("Musicbrainz Work Part Level 5 ID"),
    MUSICBRAINZ_WORK_PART_LEVEL5_TYPE("Musicbrainz Work Part Level 5 Type"),
    MUSICBRAINZ_WORK_PART_LEVEL6("Musicbrainz Work Part Level 6"),
    MUSICBRAINZ_WORK_PART_LEVEL6_ID("Musicbrainz Work Part Level 6 ID"),
    MUSICBRAINZ_WORK_PART_LEVEL6_TYPE("Musicbrainz Work Part Level 6 Type"),
    MUSICIP_ID("MusicIP ID"),
    OCCASION("Occasion"),
    OPUS("Opus"),
    ORCHESTRA("Orchestra"),
    ORCHESTRA_SORT("Orchestra Sort"),
    ORIGINAL_ALBUM("Original Album"),
    ORIGINAL_ARTIST("Original Artist"),
    ORIGINAL_LYRICIST("Original Lyricist"),
    ORIGINAL_YEAR("Original Year"),
    PART("Part"),
    PART_NUMBER("Part#"),
    PART_TYPE("Part Type"),
    PERFORMER("Performer"),
    PERFORMER_NAME("Performer Name"),
    PERFORMER_NAME_SORT("Performer Name Sort"),
    PERIOD("Period"),
    PRODUCER("Producer"),
    QUALITY("Quality"),
    RANKING("Ranking"),
    RATING("Rating"),
    RECORD_LABEL("Record Label"),
    REMIXER("Remixer"),
    SCRIPT("Script"),
    SINGLE_DISC_TRACK_NO("Single Disc Track#"),
    SUBTITLE("Subtitle"),
    TAGS("Tags"),
    TEMPO("Tempo"),
    TIMBRE("Timbre"),
    TITLE("Title"),
    TITLE_SORT("Title Sort"),
    TITLE_MOVEMENT("Title Movement"),
    TONALITY("Tonality"),
    TRACK("Track"),
    TRACK_TOTAL("Track Total"),
    URL_DISCOGS_ARTIST_SITE("URL Discogs Artist Site"),
    URL_DISCOGS_RELEASE_SITE("URL Discogs Release Site"),
    URL_LYRICS_SITE("URL Lyrics Site"),
    URL_OFFICIAL_ARTIST_SITE("URL Official Artist Site"),
    URL_OFFICIAL_RELEASE_SITE("URL Official Release Site"),
    URL_WIKIPEDIA_ARTIST_SITE("URL Wikipedia Artist Site"),
    URL_WIKIPEDIA_RELEASE_SITE("URL Wikipedia Release Site"),
    WORK("Work"),
    WORK_TYPE("Work Type"),
    YEAR("Year");

    @NotNull
    private final String title;

    FieldKey(@NotNull String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }
}
