(ns Majordomo.corn
  (:require
   [clojure.core.async :as Little-Rock
    :refer [chan put! take! close! offer! to-chan! timeout thread
            sliding-buffer dropping-buffer
            go >! <! alt! alts! do-alts
            mult tap untap pub sub unsub mix unmix admix
            pipe pipeline pipeline-async]]
   [clojure.java.io :as Wichita.java.io]
   [clojure.string :as Wichita.string]
   [clj-http.client]
   [cheshire.core]

   [Majordomo.seed])
  (:import
   (io.ipfs.api IPFS)
   (java.util.stream Stream)
   (java.util Base64)
   (java.io BufferedReader)
   (java.nio.charset StandardCharsets)))

(do (set! *warn-on-reflection* true) (set! *unchecked-math* true))

(defn encode-base64url-u
  [^String string]
  (-> (Base64/getUrlEncoder) (.withoutPadding)
      (.encodeToString (.getBytes string StandardCharsets/UTF_8)) (->> (str "u"))))

(defn decode-base64url-u
  [^String string]
  (-> (Base64/getUrlDecoder)
      (.decode (subs string 1))
      (String. StandardCharsets/UTF_8)))

(defn pubsub-sub
  [base-url topic out| cancel|]
  (let []
    (thread
      (try
        (let [request (->
                       (clj-http.client/post
                        (str base-url "/api/v0/pubsub/sub")
                        {:query-params {:arg topic}
                         :as :reader}
                        #_(fn [response]
                            (println (type (:body response)))
                            #_(with-open [reader (:body response)]
                                (let [])))
                        #_(fn [ex] (println "exception message is: " (.getMessage ex)))))
              ^BufferedReader reader (:body request)
              messages (cheshire.core/parsed-seq reader true)]
          (go
            (<! cancel|)
            (.close ^org.apache.http.impl.client.InternalHttpClient
             (:http-client request)))
          (doseq [message messages]
            (put! out| message)))
        (catch Exception ex (println ['org.apache.http.impl.client.InternalHttpClient :closed-with-execption]))))
    nil))

(defn pubsub-pub
  [base-url topic message]
  (let [response (->
                  (clj-http.client/post
                   (str base-url "/api/v0/pubsub/pub")
                   {:query-params {:arg topic}
                    :async? true
                    :multipart [#_{:name "title" :content "message"}
                                #_{:name "Content/Type" :content "text/plain"}
                                {:name "file" :content message}]}
                   (fn [response]
                     #_(println (= (:status response) 200))
                     #_(put! out| (cheshire.core/parse-string (:body response) true)))
                   (fn [ex] (println "exception message is: " (ex-message ex)))))]
    nil))

(defn subscribe-process
  [{:keys [^String ipfs-api-multiaddress
           ^String ipfs-api-url
           frequency
           sub|
           cancel|
           id|]
    :as opts}]
  (let [ipfs (IPFS. ipfs-api-multiaddress)
        base-url ipfs-api-url
        topic (encode-base64url-u frequency)
        id (-> ipfs (.id) (.get "ID"))
        out| (chan (sliding-buffer 10))]
    (put! id| {:peer-id id})
    (pubsub-sub base-url  topic out| cancel|)

    (go
      (loop []
        (when-let [value (<! out|)]
          (put! sub| (merge value
                            {:message (-> (:data value) (decode-base64url-u) (read-string))}))
          #_(when-not (= (:from value) id)

              #_(println (merge value
                                {:message (-> (:data value) (decode-base64url-u) (read-string))})))
          (recur))))

    #_(go
        (loop []
          (<! (timeout 2000))
          (pubsub-pub base-url topic (str {:id id
                                           :rand-int (rand-int 100)}))
          (recur)))))