require 'sequel'

module Helpers
  module Sequel
    extend self

    def database
      @db ||= Sequel.connect Rails.configuration.database_configuration["test"]
    end

  end
end
