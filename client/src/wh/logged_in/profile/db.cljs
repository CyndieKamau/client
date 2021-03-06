(ns wh.logged-in.profile.db
  (:require [cljs.spec.alpha :as s]
            [clojure.string :as str]
            [wh.common.cases :as cases]
            [wh.profile.db :as profile-db]
            [wh.user.db :as user]
            [wh.util :as util]))

(def maximum-skills profile-db/maximum-skills)

(s/def ::predefined-avatar integer?)
(s/def ::custom-avatar-mode boolean?)
(s/def ::editing (s/nilable keyword?))
(s/def ::contributions (s/* (s/keys :req-un [::id ::title]
                                    :opt-un [::published])))
;; FIXME: This spec is wrong (:wh/location specs unnamespaced keys and
;; we expect namespaced ones); change that so we can reuse the
;; location spec between client and server.
;; (s/def ::location-suggestions (s/map-of integer? (s/nilable (s/coll-of :wh/location))))

(s/def ::sub-db (s/keys :req [::editing ::predefined-avatar ::custom-avatar-mode]
                        :opt [::contributions]))

(def default-db {::editing              nil
                 ::predefined-avatar    1
                 ::custom-avatar-mode   false
                 ::location-suggestions {}})

(defn ->sub-db [data]
  (into {}
        (map (fn [[k v]] [(keyword "wh.logged-in.profile.db" (name k)) v]))
        data))

(def tag-query
  [:tags {:type :tech}])

(defn user-id-page-param [db]
  (get-in db [:wh.db/page-params :id]))

(defn use-personal-id?
  [db]
  (or (not (user/admin? db))
      (empty? (user-id-page-param db))))

(defn user-id
  [db]
  (if (use-personal-id? db)
    (user/id db)
    (user-id-page-param db)))

(defn published? [db]
  (boolean (get-in db [::sub-db ::published])))

(defn toggle-published [db]
  (update-in db [::sub-db ::published] not))

(defn foreign-profile? [db]
  (not (use-personal-id? db)))
