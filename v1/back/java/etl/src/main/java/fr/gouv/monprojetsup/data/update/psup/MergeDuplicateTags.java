package fr.gouv.monprojetsup.data.update.psup;

// TODO : move absolutes paths in config file.
// TODO : change stemTags method to handle tags with multiple words.

import fr.gouv.monprojetsup.data.DataSources;
import fr.gouv.monprojetsup.data.model.tags.TagsSources;
import fr.gouv.monprojetsup.data.tools.Serialisation;
import opennlp.tools.stemmer.PorterStemmer;

import java.io.IOException;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;


public class MergeDuplicateTags {


    public static int countAccentuatedCharacters(String input) {
        if (input == null || input.isEmpty()) {
            return 0;
        }

        // Normalize the string to NFD (Normalization Form D)
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);

        // Use a regular expression to match characters with diacritics
        Pattern pattern = Pattern.compile("\\p{M}");
        int count = 0;

        // Iterate through the normalized string and count accentuated characters
        for (char c : normalized.toCharArray()) {
            if (pattern.matcher(String.valueOf(c)).find()) {
                count++;
            }
        }

        return count;
    }

    public static String removeAccents(String input) {
        return input == null ? null
                : Normalizer
                .normalize(input, Normalizer.Form.NFKD)
                .replaceAll("\\p{M}","");
    }

    // 1. Load tagsSources.json
    private static TagsSources loadTagsSources() throws IOException {
        return Serialisation.fromJsonFile(DataSources.getSourceDataFilePath(DataSources.TAGS_SOURCE_RAW_FILENAME), TagsSources.class);
    }

    // 2. Stem (then merge) tags.
    private static TagsSources stemMergeTags(TagsSources tagsSources) {
        PorterStemmer stemmer = new PorterStemmer();
        TagsSources finalTagsSources = new TagsSources();
        // Map to find back an actual word instead of the stemmed one.
        Map<String, String> stemmedToOriginalTag = new HashMap<>();

        for (String tag : tagsSources.getTags()){
            String stemmedTag = removeAccents(stemmer.stem(tag).toLowerCase());
            if (!stemmedToOriginalTag.containsKey(stemmedTag)) {
                // Add tag to correspondence table and to final map.
                stemmedToOriginalTag.put(stemmedTag, tag);
                finalTagsSources.add(tag, tagsSources.sources().get(tag));
            } else {
                // 1. Remove first tag from final.
                String oldTag = stemmedToOriginalTag.get(stemmedTag);
                finalTagsSources.remove(oldTag);

                // 2. Merge first and second tags flcodes.
                Set<String> flcodes = new HashSet<>();
                flcodes.addAll(tagsSources.sources().get(oldTag));
                flcodes.addAll(tagsSources.sources().get(tag));

                // 3. Add merged tags to final.
                // Check which tag has more accentuated characters.
                if (countAccentuatedCharacters(oldTag) > countAccentuatedCharacters(tag)) {
                    finalTagsSources.add(oldTag, flcodes);
                } else {
                    finalTagsSources.add(tag, flcodes);
                }
            }
        }

        return finalTagsSources;
    }

    public static void main(String[] args) throws IOException {
        TagsSources tagsSources = loadTagsSources();

        // Stem and merge tags.
        TagsSources stemmedTagsSources = stemMergeTags(tagsSources);

        // Save stemmed tags in json file.
        Serialisation.toJsonFile(DataSources.getSourceDataFilePath(DataSources.TAGS_SOURCE_MERGED_FILENAME), stemmedTagsSources, true);

//        PorterStemmer stemmer = new PorterStemmer();
//        System.out.println(stemmer.stem("Théâtre"));
//        System.out.println(stemmer.stem("Théatre"));


    }
}