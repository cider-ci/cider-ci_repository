; Copyright © 2016 - 2017 Dr. Thomas Schank <Thomas.Schank@AlgoCon.ch>
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.

(ns cider-ci.server.web
  (:refer-clojure :exclude [str keyword])
  (:require [cider-ci.utils.core :refer [keyword str]])

  (:require
    [cider-ci.server.push]

    [cider-ci.ui2.root :as root]
    [cider-ci.ui2.ui.navbar.release :as navbar.release]
    [cider-ci.utils.config :as config :refer [get-config]]

    [cider-ci.utils.status :as status]
    [cider-ci.auth.http-basic :as http-basic]
    [cider-ci.auth.session :as auth.session]
    [cider-ci.utils.routing :as routing]
    [cider-ci.utils.ring]

    [clojure.data.json :as json]
    [compojure.core :as cpj]
    [config.core :refer [env]]
    [hiccup.page :refer [include-js include-css html5]]
    [ring.middleware.cookies]
    [ring.middleware.json]
    [ring.middleware.params]

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug :refer [I> I>> identity-with-logging]]
    [logbug.ring :refer [wrap-handler-with-logging]]
    [logbug.thrown :as thrown]

    ))

(defn dead-end-handler [req]
  {:status 404
   :body "Not found!"})

;;; HTML ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def CONTEXT "/cider-ci/server")

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (str CONTEXT (if (env :dev)
                               "/css/site.css"
                               "/css/site.min.css")))])

(defn mount-target []
  [:div#app
   [:div.container-fluid
    (if (env :dev)
      [:div.alert.alert-warning
       [:h3 "ClojureScript has not been compiled!"]
       [:p "This page depends on JavaScript!"]
       [:p "Please run " [:b "lein figwheel"] " in order to start the compiler!"]]
      [:div.alert.alert-warning
       [:h3 "JavaScript seems to be disabled or missing!"]
       [:p (str "Due to the dynamic nature of Cider-CI "
                "most pages will not work as expected without JavaScript!")]])
    (root/page)]])

(defn navbar [release]
  [:div.navbar.navbar-default {:role :navigation}
   [:div.container-fluid
    [:div.navbar-header
     [:a.navbar-brand {:href "/cider-ci/ui2/"}
      (navbar.release/navbar-release release)]]
    [:div#nav]]])

(defn html [req]
  (html5
    (head)
    [:body {:class "body-container"
            :data-user (-> req :authenticated-user
                           (select-keys [:login :is_admin]) json/write-str)
            :data-authproviders (->> (get-config) :authentication_providers
                                     (map (fn [[k v]] [k (:name v)]))
                                     (into {}) json/write-str)}
     [:div.container-fluid
      (navbar (-> (cider-ci.utils.self/release) atom))
      (mount-target)
      (include-css (str "https://maxcdn.bootstrapcdn.com/"
                        "font-awesome/4.6.3/css/font-awesome.min.css"))
      (include-js (str CONTEXT "/js/app.js"))]]))


;;; routes ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def routes
  (cpj/routes
    (cpj/ANY "*" [] html)))

(defn build-main-handler [context]
  (I> wrap-handler-with-logging
      html
      cider-ci.server.push/wrap
      (http-basic/wrap {:service true :user true})
      (auth.session/wrap :anti-forgery true)
      ;cider-ci.utils.ring/wrap-keywordize-request
      ring.middleware.json/wrap-json-response
      ring.middleware.cookies/wrap-cookies
      (ring.middleware.json/wrap-json-body {:keywords? true})
      ring.middleware.params/wrap-params
      (ring.middleware.defaults/wrap-defaults {:static {:resources "public"}})
      status/wrap
      ;(routing/wrap-prefix context)
      ))

;#### debug ###################################################################
;(logging-config/set-logger! :level :debug)
;(logging-config/set-logger! :level :info)
;(debug/debug-ns 'cider-ci.auth.http-basic)
;(debug/debug-ns 'cider-ci.auth.session)
(debug/debug-ns *ns*)
