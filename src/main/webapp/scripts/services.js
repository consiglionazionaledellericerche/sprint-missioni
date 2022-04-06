'use strict';

/* Services */

missioniApp.factory('LanguageService', function ($http, $translate, LANGUAGES) {
        return {
            getBy: function(language) {
                if (language == undefined) {
                    language = $translate.storage().get('NG_TRANSLATE_LANG_KEY');
                }
                if (language == undefined) {
                    language = 'it';
                }

                var promise =  $http.get('i18n/' + language + '.json').then(function(response) {
                    return LANGUAGES;
                });
                return promise;
            }
        };
    });

missioniApp.factory('Register', function ($resource) {
        return $resource('app/rest/register', {}, {
        });
    });

missioniApp.factory('Activate', function ($resource) {
        return $resource('app/rest/activate', {}, {
            'get': { method: 'GET', params: {}, isArray: false}
        });
    });

missioniApp.factory('MissioniRespinte', function ($http) {
        return {
            findHistoryMissioniRespinte: function(tipoMissione,  idMissione) {
                var promise = $http.get('api/rest/missioniRespinte/history', {params: {tipoMissione : tipoMissione, 
                        idMissione: idMissione}}).then(function (response) {
                    return response.data;
                });
                return promise;
            }    
        }    
    });

missioniApp.factory('Account', function ($resource) {
        return $resource('api/current-account', {}, {
        });
    });

missioniApp.factory('AccountFromToken', function ($resource) {
        return $resource('api/rest/ldap/account/token', {}, {
            'get': { method: 'GET', params: {}, isArray: false}
        });
    });

missioniApp.factory('AccountLDAP', function ($resource) {
        return $resource('api/current-account', {}, {
        });
    });

missioniApp.factory('Password', function ($resource) {
        return $resource('app/rest/account/change_password', {}, {
        });
    });

missioniApp.factory('Sessions', function ($resource) {
        return $resource('app/rest/account/sessions/:series', {}, {
            'get': { method: 'GET', isArray: true}
        });
    });

missioniApp.factory('MetricsService',function ($http) {
            return {
            get: function() {
                var promise = $http.get('metrics/metrics').then(function(response){
                    return response.data;
                });
                return promise;
            }
        };
    });

missioniApp.factory('ThreadDumpService', function ($http) {
        return {
            dump: function() {
                var promise = $http.get('dump').then(function(response){
                    return response.data;
                });
                return promise;
            }
        };
    });

missioniApp.factory('HealthCheckService', function ($rootScope, $http) {
        return {
            check: function() {
                var promise = $http.get('health').then(function(response){
                    return response.data;
                });
                return promise;
            }
        };
    });

missioniApp.factory('ConfigurationService', function ($rootScope, $filter, $http) {
    return {
        get: function() {
            var promise = $http.get('configprops').then(function(response){
                var properties = [];
                angular.forEach(response.data, function(data) {
                    properties.push(data);
                });
                var orderBy = $filter('orderBy');
                return orderBy(properties, 'prefix');;
            });
            return promise;
        }
    };
});

missioniApp.factory('LogsService', function ($resource) {
        return $resource('app/rest/logs', {}, {
            'findAll': { method: 'GET', isArray: true},
            'changeLevel':  { method: 'PUT'}
        });
    });

missioniApp.factory('DateService', function ($http, DateUtils) {
    return {
        today: function() {
                var promise = $http.get('api/rest/date/today');
                var res = promise.then (function (result) {
                    var data = result.data;
                    data = data.replace(new RegExp('"', 'g'), '');
                    return DateUtils.convertDateTimeFromServer(data);
                });
                return res;
            }
        };
    });

'use strict';

missioniApp.factory('AuditsService', function ($http) {
        return {
            findAll: function() {
                var promise = $http.get('app/rest/audits/all').then(function (response) {
                    return response.data;
                });
                return promise;
            },
            findByDates: function(fromDate, toDate) {
                var promise = $http.get('app/rest/audits/byDates', {params: {fromDate: fromDate, toDate: toDate}}).then(function (response) {
                    return response.data;
                });
                return promise;
            }
        }
    });

missioniApp.factory('Session', function (ProxyService) {
        this.create = function (login, matricola, firstName, lastName, email, userRoles, allUoForUsersSpecial, uoForUsersSpecial, isAccountLDAP, comune_nascita, data_nascita, comune_residenza, indirizzo_residenza, num_civico_residenza, cap_residenza, provincia_residenza, codice_fiscale, profilo, struttura_appartenenza, codice_sede, codice_uo, livello) {
            this.login = login;
            this.matricola = matricola;
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
            this.userRoles = userRoles;
            this.comune_nascita = comune_nascita;
            this.data_nascita = data_nascita;
            this.comune_residenza = comune_residenza;

            this.indirizzo_residenza = indirizzo_residenza;
            this.num_civico_residenza = num_civico_residenza;
            if (this.num_civico_residenza){
                this.indirizzo_completo_residenza = this.indirizzo_residenza+" "+this.num_civico_residenza;
            } else {
                if (this.indirizzo_residenza){
                    this.indirizzo_completo_residenza = this.indirizzo_residenza;
                } else {
                    this.indirizzo_completo_residenza = null;
                }
            }
            this.cap_residenza = cap_residenza;
            this.provincia_residenza = provincia_residenza;
            this.codice_fiscale = codice_fiscale;
            if (profilo){
                this.profilo = profilo.trim();
            }
            if (codice_uo){
                this.codice_uo = codice_uo;
            } else {
                this.codice_uo = "ZZZZZZ";
            }
            if (struttura_appartenenza){
                this.struttura_appartenenza = struttura_appartenenza;
            } else {
                this.struttura_appartenenza = "ZZZ.ZZZ";
            }
            this.codice_sede = codice_sede;
            this.livello = livello;
            this.allUoForUsersSpecial = allUoForUsersSpecial;
            this.uoForUsersSpecial = uoForUsersSpecial;
            this.abilitatoRendereDefinitivo = null;
            if (uoForUsersSpecial){
                for (var i=0; i<uoForUsersSpecial.length; i++) {
                    if (uoForUsersSpecial[i].rendi_definitivo === "S"){
                        this.abilitatoRendereDefinitivo = true;
                    }
                }

            }
            this.livello = livello;

            this.isAccountLDAP = isAccountLDAP;
        };
        this.invalidate = function () {
            this.login = null;
            this.matricola = null;
            this.firstName = null;
            this.lastName = null;
            this.email = null;
            this.userRoles = null;
            this.comune_nascita = null;
            this.data_nascita = null;
            this.comune_residenza = null;
            this.indirizzo_residenza = null;
            this.num_civico_residenza = null;
            this.indirizzo_completo_residenza = null;
            this.cap_residenza = null;
            this.provincia_residenza = null;
            this.codice_fiscale = null;
            this.profilo = null;
            this.struttura_appartenenza = null;
            this.codice_sede = null;
            this.codice_uo = null;
            this.livello = null;
            this.allUoForUsersSpecial = null;
            this.uoForUsersSpecial = null;
            this.isAccountLDAP = null;
            this.abilitatoRendereDefinitivo = null;
        };
        return this;
    });

missioniApp.factory('AuthenticationSharedService', function (ProxyService, $rootScope, $http, authService, Session, Account, AccountLDAP, Base64Service, AccessToken, AccountFromToken, $sessionStorage, DateUtils, AuthServerProvider, $location) {
    var today = new Date();
    var recuperoResidenza = function(data){
        if (data.comune_residenza){
            return data.comune_residenza;
        } else {
            if (data.codice_fiscale){
                var today = new Date();
                var x = ProxyService.getTerzoPerCompenso(data.codice_fiscale, today, today);
                var y = x.then(function (result) {
                    if (result && result.data && result.data.elements && result.data.elements.length > 0){
                        var terziPerCompenso = result.data.elements;
                        var terzoPerCompenso = terziPerCompenso[0];
                        return terzoPerCompenso.ds_comune_fiscale;
                    }
                    return null;
                });
                x.error(function (result) {
                    return null;
                });
                return y;
            } else {
                return null;
            }
        }
    }

    var recuperoDatiTerzo = function(data){
        if (data.codice_fiscale){
            var x = ProxyService.getTerzoPerCompenso(data.codice_fiscale, today, today);
            var y = x.then(function (result) {
                if (result && result.data && result.data.elements && result.data.elements.length > 0){
                    var terziPerCompenso = result.data.elements;
                    var terzoPerCompenso = terziPerCompenso[0];
                    return terzoPerCompenso;
                }
                return null;
            });
            x.error(function (result) {
                return null;
            });
            return y;
        } else {
            return null;
        }
    }

    var menuSso = function(isFromKeycloak){
        if (isFromKeycloak){
          var targetElement = '#navbar-collapse > ul';
          if (screen.width < 768) {
             targetElement = '.sso-cnr-menu';
          }
          CreateSsoCnrMenu.createAppsMenuAndButton(targetElement, {'placement': 'right'});
          CreateSsoCnrMenu.createUserMenuAndButton(targetElement, {
                 'placement': 'right',
                 'login': $sessionStorage.account.login,
                 'name': $sessionStorage.account.firstName + ' ' + $sessionStorage.account.lastName,
                 'logoutCallback': function (e) {
                 location.href = '/sso/logout';
              }
          });
        }
    }

    var gestioneUtente = function(utente, isFromKeycloak){
                            if (utente.struttura_appartenenza || utente.uid=="app.missioni" || utente.profilo ) {
                                if (!isFromKeycloak){
                                    httpHeaders.common['X-Proxy-Authorization'] = 'Basic ' + Base64Service.encode(utente.uid.toLowerCase() + ':' + param.password);
                                }
                                $http.get(
                                    'api/account-info?username=' + utente.uid.toLowerCase()
                                ).success(function (data, status, headers, config) {
                                    if (!isFromKeycloak){
                                        delete httpHeaders.common['X-Proxy-Authorization'];
                                    }
                                    var comune_residenza = null;
                                    if (utente.uid.toLowerCase() == "app.missioni"){
                                        Session.create(utente.uid.toLowerCase(), null, data.nome, data.cognome, data.email_comunicazioni, data.roles, data.allUoForUsersSpecial, data.uoForUsersSpecial, true);
                                        $rootScope.account = Session;
                                        $sessionStorage.account = Session;
                                        authService.loginConfirmed(data);
                                        menuSso(isFromKeycloak);
                                    } else {
                                        recuperoDatiTerzo(data).then(function (result){
                                            if (data.struttura_appartenenza || data.sigla_sede) {
                                                var sede = "";
                                                if (data.struttura_appartenenza){
                                                    sede = data.struttura_appartenenza;
                                                } else {
                                                    sede = data.sigla_sede;
                                                }
                                                if (data.comune_residenza){
                                                    comune_residenza = data.comune_residenza;
                                                    if (result && data.matricola && result.ti_dipendente_altro == 'D' && ((data.data_cessazione && DateUtils.convertDateTimeFromServer(data.data_cessazione) >= today) || (!data.data_cessazione))){
                                                        Session.create(utente.uid.toLowerCase(), data.matricola, data.nome, data.cognome, data.email_comunicazioni, data.roles,
                                                                    data.allUoForUsersSpecial, data.uoForUsersSpecial, true,
                                                                    data.comune_nascita, data.data_nascita, comune_residenza, data.indirizzo_residenza,
                                                                    data.num_civico_residenza, data.cap_residenza, data.provincia_residenza, data.codice_fiscale,
                                                                    data.profilo, sede, data.codice_sede, data.codice_uo, data.livello_profilo);
                                                        $rootScope.account = Session;
                                                        $sessionStorage.account = Session;
                                                        menuSso(isFromKeycloak);
                                                    } else {
                                                        var matr = null;
                                                        var profilo = null;
                                                        if (result && result.ti_dipendente_altro == 'A'){
                                                            matr = "";
                                                            comune_residenza = result.ds_comune_fiscale;
                                                            profilo = result.ds_tipo_rapporto;
                                                        } else {
                                                            matr = data.matricola;
                                                            profilo = data.profilo;
                                                        }
                                                        Session.create(utente.uid.toLowerCase(), matr, data.nome, data.cognome, data.email_comunicazioni, data.roles,
                                                                data.allUoForUsersSpecial, data.uoForUsersSpecial, true,
                                                                data.comune_nascita, data.data_nascita, comune_residenza, data.indirizzo_residenza,
                                                                data.num_civico_residenza, data.cap_residenza, data.provincia_residenza, data.codice_fiscale,
                                                                profilo, sede, data.codice_sede, data.codice_uo, null);
                                                        $rootScope.account = Session;
                                                        $sessionStorage.account = Session;
                                                        menuSso(isFromKeycloak);
                                                    }
                                                } else {
                                                    recuperoResidenza(data).then(function (result){
                                                            comune_residenza = result;
                                                            Session.create(utente.uid.toLowerCase(), data.matricola, data.nome, data.cognome, data.email_comunicazioni, data.roles,
                                                                    data.allUoForUsersSpecial, data.uoForUsersSpecial, true,
                                                                data.comune_nascita, data.data_nascita, comune_residenza, data.indirizzo_residenza,
                                                                data.num_civico_residenza, data.cap_residenza, data.provincia_residenza, data.codice_fiscale,
                                                                data.profilo, sede, data.codice_sede, data.codice_uo, data.livello_profilo);
                                                            $rootScope.account = Session;
                                                            $sessionStorage.account = Session;
                                                            menuSso(isFromKeycloak);
                                                    });
                                                }
                                            } else {
                                                if (isFromKeycloak){
                                                    Session.create(utente.uid.toLowerCase(), null, data.nome, data.cognome, data.email_comunicazioni, data.roles, data.allUoForUsersSpecial, data.uoForUsersSpecial, true);
                                                } else {
                                                    Session.create(utente.uid.toLowerCase(), null, data.nome, data.cognome, data.email_comunicazioni, ['ROLE_USER'], data.allUoForUsersSpecial, data.uoForUsersSpecial, true);
                                                }
                                                $rootScope.account = Session;
                                                $sessionStorage.account = Session;
                                                menuSso(isFromKeycloak);
                                            }
                                            authService.loginConfirmed(data);
                                        });
                                    }
                                }).error(function (data, status, headers, config) {
                                    if (!isFromKeycloak){
                                        delete httpHeaders.common['X-Proxy-Authorization'];
                                    }
                                });
                            } else {
                                $rootScope.salvataggio = true;
                                Account.get(function(data) {
                                    $rootScope.salvataggio = false;
                                    if (isFromKeycloak){
                                       Session.create(data.uid, null, data.nome, data.cognome, data.email_comunicazioni, data.roles);
                                    } else {
                                        Session.create(data.uid, null, data.firstName, data.lastName, data.email, data.roles);
                                    }
                                    $rootScope.account = Session;
                                    $sessionStorage.account = Session;
                                    menuSso(isFromKeycloak);
                                    authService.loginConfirmed(data);
                                    if (isFromKeycloak){
                                        $location.path('/error').replace();
                                    }
                                });
                            }
    }

        return {
            login: function (param) {
                $rootScope.isUserNotKeycloak = false;
                $rootScope.isUserKeycloak = false;
                AuthServerProvider.profileInfo().then(function (profile) {
                    if (profile.data.keycloakEnabled) {
                        $rootScope.isUserKeycloak = true;
                    } else {
                        $rootScope.isUserNotKeycloak = true;
                    }
                    if (!$rootScope.isUserKeycloak){
                        var data = {
                            username: param.username.toLowerCase(),
                            password: param.password,
                            rememberMe: "true"
                        };
                        $http.post('api/authenticate', data, {
                            headers: {
                                "Content-Type": "application/json",
                                "Accept": "application/json"
                            },
                            ignoreAuthModule: 'ignoreAuthModule'
                        }).success(function (data, status, headers, config) {
                            httpHeaders.common['Authorization'] = 'Bearer ' + data.id_token;
                            AccessToken.set(data);
                            $rootScope.salvataggio = true;
                            AccountLDAP.get(function(data) {
                                $rootScope.salvataggio = false;
                                gestioneUtente(data, false);
                            });
                        }).error(function (data, status, headers, config) {
                            $rootScope.authenticated = false;
                            $rootScope.authenticationError = true;
                            Session.invalidate();
                            AccessToken.remove();
                            delete httpHeaders.common['Authorization'];
                            $rootScope.$broadcast('event:auth-loginRequired', data);
                        });
                    } else {
                        gestioneUtente(param.user, true);
                    }
                });
            },
            valid: function (authorizedRoles) {
                var token = AccessToken.get();
                if(token !== null) {
                    httpHeaders.common['Authorization'] = 'Bearer ' + token;
                }

                $http.get('protected/authentication_check.gif', {
                    ignoreAuthModule: 'ignoreAuthModule'
                }).success(function (data, status, headers, config) {
                    if (!Session.login || token != undefined) {
                        if (token == undefined || AccessToken.expired()) {
                            $rootScope.$broadcast("event:auth-loginRequired");
                            return;
                        }
                        $rootScope.salvataggio = true;
                        AccountLDAP.get(function(data) {
                        $rootScope.salvataggio = false;
                            if (!data.struttura_appartenenza && data.uid != "app.missioni" && !data.profilo) {
                                $rootScope.salvataggio = true;
                                Account.get(function(data) {
                                    $rootScope.salvataggio = false;
                                    Session.create(data.uid, null, data.firstName, data.lastName, data.email, data.authorities);
                                    $rootScope.account = Session;
                                    $sessionStorage.account = Session;
                                    if (!$rootScope.isAuthorized(authorizedRoles)) {
                                        // user is not allowed
                                       $rootScope.$broadcast("event:auth-notAuthorized");
                                    } else {
                                        $rootScope.$broadcast("event:auth-loginConfirmed");
                                    }
                                });
                            } else {

                                        var comune_residenza = null;
                                        if (data.uid == "app.missioni"){
                                            Session.create(data.uid, null, data.nome, data.cognome, data.email_comunicazioni, data.roles, data.allUoForUsersSpecial, data.uoForUsersSpecial, true);
                                            $rootScope.account = Session;
                                            $sessionStorage.account = Session;

                                            if (!$rootScope.isAuthorized(authorizedRoles)) {
                                                // user is not allowed
                                                $rootScope.$broadcast("event:auth-notAuthorized");
                                            } else {
                                                $rootScope.$broadcast("event:auth-loginConfirmed");
                                            }
                                        } else {
                                            recuperoDatiTerzo(data).then(function (result){
                                            if (data.struttura_appartenenza || data.sigla_sede) {
                                                var sede = "";
                                                if (data.struttura_appartenenza){
                                                    sede = data.struttura_appartenenza;
                                                } else {
                                                    sede = data.sigla_sede;
                                                }
                                                    if (data.comune_residenza){
                                                        comune_residenza = data.comune_residenza;
                                                        if (result && data.matricola && result.ti_dipendente_altro == 'D' && ((data.data_cessazione && DateUtils.convertDateTimeFromServer(data.data_cessazione) >= today) || (!data.data_cessazione))){
                                                            Session.create(data.uid, data.matricola, data.nome, data.cognome, data.email_comunicazioni, data.roles, 
                                                                        data.allUoForUsersSpecial, data.uoForUsersSpecial, true,
                                                                        data.comune_nascita, data.data_nascita, comune_residenza, data.indirizzo_residenza,
                                                                        data.num_civico_residenza, data.cap_residenza, data.provincia_residenza, data.codice_fiscale,
                                                                        data.profilo, sede, data.codice_sede, data.codice_uo, data.livello_profilo);

                                                        } else {
                                                            var matr = null;
                                                            var profilo = null;    
                                                            if (result && result.ti_dipendente_altro == 'A'){
                                                                matr = "";
                                                                comune_residenza = result.ds_comune_fiscale;
                                                                profilo = result.ds_tipo_rapporto;
                                                            } else {
                                                                matr = data.matricola;
                                                                profilo = data.profilo;
                                                            }
                                                            Session.create(data.uid, matr, data.nome, data.cognome, data.email_comunicazioni, data.roles, 
                                                                        data.allUoForUsersSpecial, data.uoForUsersSpecial, true,
                                                                        data.comune_nascita, data.data_nascita, comune_residenza, data.indirizzo_residenza,
                                                                        data.num_civico_residenza, data.cap_residenza, data.provincia_residenza, data.codice_fiscale,
                                                                        profilo, sede, data.codice_sede, data.codice_uo, null);
                                                        }
                                                    } else {
                                                        recuperoResidenza(data).then(function (result){
                                                                comune_residenza = result;
                                                                Session.create(data.uid, data.matricola, data.nome, data.cognome, data.email_comunicazioni, data.roles, 
                                                                    data.allUoForUsersSpecial, data.uoForUsersSpecial, true,
                                                                    data.comune_nascita, data.data_nascita, comune_residenza, data.indirizzo_residenza,
                                                                    data.num_civico_residenza, data.cap_residenza, data.provincia_residenza, data.codice_fiscale,
                                                                    data.profilo, sede, data.codice_sede, data.codice_uo, data.livello_profilo);
                                                            });
                                                    }    
                                                } else {
                                                    Session.create(data.uid, null, data.nome, data.cognome, data.email_comunicazioni, ['ROLE_USER'], data.allUoForUsersSpecial, data.uoForUsersSpecial, true);
                                                }
                                                $rootScope.account = Session;
                                                $sessionStorage.account = Session;

                                                if (!$rootScope.isAuthorized(authorizedRoles) && (token == undefined || AccessToken.expired())) {
                                                    // user is not allowed
                                                    $rootScope.$broadcast("event:auth-notAuthorized");
                                                } else {
                                                    $rootScope.$broadcast("event:auth-loginConfirmed");
                                                }
                                            });    
                                        }

                            }
                        });
                    }else{
                        if (!$rootScope.isAuthorized(authorizedRoles)) {
                                // user is not allowed
                                $rootScope.$broadcast("event:auth-notAuthorized");
                        } else {
                                $rootScope.$broadcast("event:auth-loginConfirmed");
                        }
                    }
                }).error(function (data, status, headers, config) {
                    if (!$rootScope.isAuthorized(authorizedRoles)) {
                        $rootScope.$broadcast('event:auth-loginRequired', data);
                    }
                });
            },
            isAuthorized: function (authorizedRoles) {
                if (!angular.isArray(authorizedRoles)) {
                    if (authorizedRoles == '*') {
                        return true;
                    }

                    authorizedRoles = [authorizedRoles];
                }

                var isAuthorized = false;
                angular.forEach(authorizedRoles, function(authorizedRole) {
                    var authorized = (!!Session.login &&
                        Session.userRoles.indexOf(authorizedRole) !== -1);

                    if (authorized || authorizedRole == '*') {
                        isAuthorized = true;
                    }
                });

                return isAuthorized;
            },
                         isAuthorizedApp: function () {
                             return Session.login && Session.userRoles && Session.userRoles.size > 0;
                         },
            logout: function () {
                $rootScope.authenticationError = false;
                $rootScope.authenticated = false;
                $rootScope.account = null;
                $sessionStorage.account = null;
                var token = AccessToken.get();
                if(token !== null) {
                    AccessToken.remove();
                    $http.get('api/logout');
                } else {
                    location.href = '/sso/logout';
                }
                Session.invalidate();
                delete httpHeaders.common['Authorization'];
                authService.loginCancelled();
            },
            profileInfo: function() {
                return $http.get('api/profile/info').success(function(response) {
                    return response;
                });
            }
        };
    });
