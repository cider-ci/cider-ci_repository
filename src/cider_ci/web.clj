; Copyright © 2013 - 2016 Dr. Thomas Schank <Thomas.Schank@AlgoCon.ch>
; Licensed under the terms of the GNU Affero General Public License v3.
; See the "LICENSE.txt" file provided with this software.

(ns cider-ci.web
  (:refer-clojure :exclude [str keyword])
  (:require [cider-ci.utils.core :refer [keyword str]])
  (:require

    [compojure.core :as cpj]

    [cider-ci.repository.web :as repository.web]

    [clj-logging-config.log4j :as logging-config]
    [clojure.tools.logging :as logging]
    [logbug.debug :as debug :refer [I> I>> identity-with-logging]]
    [logbug.ring :refer [wrap-handler-with-logging]]
    [logbug.thrown :as thrown]
    ))

(defn dead-end-handler [req]
  {:status 404
   :body "Not found!"})

(def repositories-handler
  (repository.web/build-main-handler "/cider-ci/repositories"))

(def routes
  (cpj/routes
    (cpj/ANY "/cider-ci/repositories/*" [] repositories-handler)
    (cpj/ANY "*" [] dead-end-handler)
    ))

(defn build-main-handler [_]
  routes)
