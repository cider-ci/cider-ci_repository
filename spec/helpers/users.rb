module Helpers
  module Users
    extend self

    def create_default_users
      password_digest = '$2a$06$YXyaPR7IzANgRuOx5JJTzO8THVxfeQ8wVTB.NZJzySLP6Qg5v3zhW'
      @users = database[:users]
      @users.insert(login: 'normin', is_admin: false, password_digest: password_digest)
      @users.insert(login: 'admin', is_admin: true, password_digest: password_digest)
    end

  end
end
