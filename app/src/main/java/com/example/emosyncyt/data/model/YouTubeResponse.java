package com.example.emosyncyt.data.model;

import java.util.List;

public class YouTubeResponse {
    private List<Item> items;

    public List<Item> getItems() {
        return items;
    }

    public static class Item {
        private Id id;
        private Snippet snippet;

        public Id getId() {
            return id;
        }

        public Snippet getSnippet() {
            return snippet;
        }

        public static class Id {
            private String videoId;

            public String getVideoId() {
                return videoId;
            }
        }

        public static class Snippet {
            private String title;
            private Thumbnails thumbnails;

            public String getTitle() {
                return title;
            }

            public Thumbnails getThumbnails() {
                return thumbnails;
            }

            public static class Thumbnails {
                private ThumbnailDetail medium;

                public ThumbnailDetail getMedium() {
                    return medium;
                }

                public static class ThumbnailDetail {
                    private String url;

                    public String getUrl() {
                        return url;
                    }
                }
            }
        }
    }
}
