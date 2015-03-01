(ns cider-ci.repository.main
  (:require 
    [cider-ci.auth.core :as auth]
    [cider-ci.repository.repositories :as repositories]
    [cider-ci.repository.web :as web]
    [cider-ci.utils.config :as config :refer [get-config]]
    [cider-ci.utils.http :as http]
    [cider-ci.utils.map :refer [deep-merge]]
    [cider-ci.utils.messaging :as messaging]
    [cider-ci.utils.nrepl :as nrepl]
    [cider-ci.utils.rdbms :as rdbms]
    [cider-ci.utils.with :as with]
    [clojure.tools.logging :as logging]
    ))

(defn get-db-spec []
  (let [conf (get-config)]
    (deep-merge 
      (or (-> conf :database ) {} )
      (or (-> conf :services :dispatcher :database ) {} ))))

(defn -main [& args]
  (with/logging 
    (config/initialize ["../config/config_default.yml" "./config/config_default.yml" "./config/config.yml"])
    (nrepl/initialize (-> (get-config) :services :repository :nrepl))
    (http/initialize (select-keys (get-config) [:basic_auth]))
    (messaging/initialize (:messaging (get-config)))
    (rdbms/initialize (get-db-spec))
    (auth/initialize (select-keys (get-config) [:session :basic_auth :secret]))
    (repositories/initialize (-> (get-config) :services :repository :repositories))
    (web/initialize (get-config))))
