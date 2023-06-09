'use strict';

/* Controllers */

missioniApp.controller('MainController', function ($scope, $sessionStorage, $location, ElencoOrdiniMissioneService, AccessToken) {
    AccessToken.remove();
});

missioniApp.controller('HomeController', function ($scope, $sessionStorage, $location, ui, ElencoOrdiniMissioneService, ElencoRimborsiMissioneService, ConfigService, DateService, ProxyService) {
    $scope.reloadUoWork = function(uo){
        if (uo){
            $scope.uoWorkForSpecialUser = uo;
        }
        $scope.accountModel = null;
        $scope.elencoPersone = [];
        $scope.ordiniMissione = [];
        $scope.messageOrdiniNonEsistenti = false;
        $scope.userWork = null;
        if (uo){
            $scope.disableUo = true;
            var persons = ProxyService.getPersons(uo).then(function(result){
                if (result ){
                    $scope.elencoPersone = result;
                    $scope.disableUo = false;
                }
            });
        }
    }

    var recuperoMessaggio = function(){
                var x = ConfigService.getMessage();
                var y = x.then(function (result) {
                    if (result){
                        $scope.message = result;
                    }
                });
    }

    var recuperoMissioni = function(uo){
        ElencoOrdiniMissioneService.findListAnnullamentiToValidate(uo).then(function(response){
            $scope.listAnnullamentiOrdiniMissioniToValidate = response;
            $scope.esistonoAnnullamentiDaApprovare = false;
            $scope.esistonoAnnullamentiApprovati = false;
            $scope.esistonoAnnullamentiRespinti = false;
            $scope.esistonoAnnullamentiDaConfermare = false;
            $scope.esistonoAnnullamentiDaValidare = false;
            if ($scope.listAnnullamentiOrdiniMissioniToValidate){
                for (var i=0; i< $scope.listAnnullamentiOrdiniMissioniToValidate.length; i++) {
                    if ($scope.listAnnullamentiOrdiniMissioniToValidate[i].statoFlussoRitornoHome == 'D'){
                        $scope.esistonoAnnullamentiDaApprovare = true;
                    } else if ($scope.listAnnullamentiOrdiniMissioniToValidate[i].statoFlussoRitornoHome == 'R'){
                        $scope.esistonoAnnullamentiRespinti = true;
                    } else if ($scope.listAnnullamentiOrdiniMissioniToValidate[i].statoFlussoRitornoHome == 'C'){
                        $scope.esistonoAnnullamentiDaConfermare = true;
                    } else if ($scope.listAnnullamentiOrdiniMissioniToValidate[i].statoFlussoRitornoHome == 'A'){
                        $scope.esistonoAnnullamentiApprovati = true;
                    } else if ($scope.listAnnullamentiOrdiniMissioniToValidate[i].statoFlussoRitornoHome == 'V'){
                        $scope.esistonoAnnullamentiDaValidare = true;
                    }
                }
            }
            $scope.endSearchCmisAnnullamenti = true;
        },
        function(error){
            $scope.endSearchCmisAnnullamenti = true;
        });        

        ElencoOrdiniMissioneService.findListToValidate(uo).then(function(response){
            $scope.listOrdiniMissioniToValidate = response.data;
            $scope.esistonoOrdiniDaRendereDefinitivi = false;
            $scope.esistonoOrdiniDaApprovare = false;
            $scope.esistonoOrdiniAnnullati = false;
            $scope.esistonoOrdiniApprovati = false;
            $scope.esistonoOrdiniRespinti = false;
            $scope.esistonoOrdiniDaConfermare = false;
            $scope.esistonoOrdiniDaValidare = false;
            $scope.esistonoOrdiniResponsabileGruppo = false;
            if ($scope.listOrdiniMissioniToValidate){
                for (var i=0; i< $scope.listOrdiniMissioniToValidate.length; i++) {
                    if ($scope.listOrdiniMissioniToValidate[i].statoFlussoRitornoHome == 'D'){
                        $scope.esistonoOrdiniDaApprovare = true;
                    } else if ($scope.listOrdiniMissioniToValidate[i].statoFlussoRitornoHome == 'R'){
                        $scope.esistonoOrdiniRespinti = true;
                    } else if ($scope.listOrdiniMissioniToValidate[i].statoFlussoRitornoHome == 'C'){
                        $scope.esistonoOrdiniDaConfermare = true;
                    } else if ($scope.listOrdiniMissioniToValidate[i].statoFlussoRitornoHome == 'A'){
                        $scope.esistonoOrdiniApprovati = true;
                    } else if ($scope.listOrdiniMissioniToValidate[i].statoFlussoRitornoHome == 'N'){
                        $scope.esistonoOrdiniAnnullati = true;
                    } else if ($scope.listOrdiniMissioniToValidate[i].statoFlussoRitornoHome == 'V'){
                        $scope.esistonoOrdiniDaValidare = true;
                    } else if ($scope.listOrdiniMissioniToValidate[i].statoFlussoRitornoHome == 'F'){
                        $scope.esistonoOrdiniDaRendereDefinitivi = true;
                    } else if ($scope.listOrdiniMissioniToValidate[i].statoFlussoRitornoHome == 'M'){
                        $scope.esistonoOrdiniResponsabileGruppo = true;
                    }
                }
            }
            $scope.endSearchCmisOrdine = true;
        },
        function(error){
            $scope.endSearchCmisOrdine = true;
        });        

        ElencoRimborsiMissioneService.findListToValidate(uo).then(function(response){
            $scope.listRimborsiMissioniToValidate = response.data;
            $scope.esistonoRimborsiDaApprovare = false;
            $scope.esistonoRimborsiAnnullati = false;
            $scope.esistonoRimborsiApprovati = false;
            $scope.esistonoRimborsiRespinti = false;
            $scope.esistonoRimborsiDaConfermare = false;
            if ($scope.listRimborsiMissioniToValidate){
                for (var i=0; i< $scope.listRimborsiMissioniToValidate.length; i++) {
                    if ($scope.listRimborsiMissioniToValidate[i].statoFlussoRitornoHome == 'D'){
                        $scope.esistonoRimborsiDaApprovare = true;
                    } else if ($scope.listRimborsiMissioniToValidate[i].statoFlussoRitornoHome == 'R'){
                        $scope.esistonoRimborsiRespinti = true;
                    } else if ($scope.listRimborsiMissioniToValidate[i].statoFlussoRitornoHome == 'C'){
                        $scope.esistonoRimborsiDaConfermare = true;
                    } else if ($scope.listRimborsiMissioniToValidate[i].statoFlussoRitornoHome == 'A'){
                        $scope.esistonoRimborsiApprovati = true;
                    } else if ($scope.listRimborsiMissioniToValidate[i].statoFlussoRitornoHome == 'N'){
                        $scope.esistonoRimborsiAnnullati = true;
                    } else if ($scope.listRimborsiMissioniToValidate[i].statoFlussoRitornoHome == 'V'){
                        $scope.esistonoRimborsiDaValidare = true;
                    } else if ($scope.listRimborsiMissioniToValidate[i].statoFlussoRitornoHome == 'VA'){
                        $scope.esistonoRimborsiAllaValidazioneAmm = true;
                    }
                }
            }
            $scope.endSearchCmisRimborso = true;
        },
        function(error){
            $scope.endSearchCmisRimborso = true;
        });        
   
        ElencoRimborsiMissioneService.findListAnnullamentiToValidate(uo).then(function(response){
            $scope.listAnnullamentiRimborsiMissioniToValidate = response;
            $scope.esistonoAnnullamentiRimborsiDaConfermare = false;
            if ($scope.listAnnullamentiRimborsiMissioniToValidate){
                for (var i=0; i< $scope.listAnnullamentiRimborsiMissioniToValidate.length; i++) {
                    $scope.esistonoAnnullamentiRimborsiDaConfermare = true;
                }
            }
            $scope.endSearchCmisAnnullamentiRimborso = true;
        },
        function(error){
            $scope.endSearchCmisAnnullamentiRimborso = true;
        });        
    }
    $scope.uoWorkForSpecialUser = null;
    $scope.endSearchCmisAnnullamenti = false;
    $scope.endSearchCmisAnnullamentiRimborso = false;
    $scope.endSearchCmisOrdine = false;
    $scope.endSearchCmisRimborso = false;
    if (!$sessionStorage.account || !$sessionStorage.account.login) {
      $location.path('/login');
    } else {
        $scope.account = $sessionStorage.account;
        recuperoMessaggio();
        var uoForUsersSpecial = $scope.account.uoForUsersSpecial;
        if (uoForUsersSpecial){
            $scope.userSpecial = true;
            var today = DateService.today().then(function(result){
                if (result){
                    var elenco = ProxyService.getUos(result.getFullYear(), null, ProxyService.buildUoRichiedenteSiglaFromUoSiper($scope.account)).then(function(result){
                        $scope.uoForUsersSpecial = [];
                        if (result && result.data){
                            var uos = result.data.elements;
                            var ind = -1;
                            for (var i=0; i<uos.length; i++) {
                                for (var k=0; k<uoForUsersSpecial.length; k++) {
                                    if (uos[i].cd_unita_organizzativa == ProxyService.buildUoSiglaFromUoSiper(uoForUsersSpecial[k].codice_uo)){
                                        ind ++;
                                        $scope.uoForUsersSpecial[ind] = uos[i];
                                    }
                                }
                            }
                            if ($scope.uoForUsersSpecial.length === 1){
                                $scope.uoWorkForSpecialUser = $scope.uoForUsersSpecial[0];
                                $scope.selectUo($scope.uoWorkForSpecialUser.cd_unita_organizzativa);
                            } else {
                                recuperoMissioni(null);
                            }
                        } else {
                            $scope.userSpecial = false;
                            recuperoMissioni(null);
                        }
                    });
                }
            });
        } else {
            $scope.userSpecial = false;
            recuperoMissioni(null);
        }
    }

    $scope.selectUo = function (uo) {
        if (uo){
            if (uo.cd_unita_organizzativa){
                recuperoMissioni(uo.cd_unita_organizzativa);
            } else {
                recuperoMissioni(uo);                
            }
        }
    };

    $scope.doSelectRimborsoMissioneValidazione = function (rimborsoMissione) {
        $location.path('/rimborso-missione/'+rimborsoMissione.id+'/'+"S");
    };
    $scope.doSelectRimborsoMissione = function (rimborsoMissione) {
        $location.path('/rimborso-missione/'+rimborsoMissione.id);
    };
    $scope.doSelectOrdineMissione = function (ordineMissione) {
        $location.path('/ordine-missione/'+ordineMissione.id);
    };
    $scope.doSelectOrdineMissioneValidazione = function (ordineMissione) {
        $location.path('/ordine-missione/'+ordineMissione.id+'/'+"S");
    };
    $scope.doSelectOrdineMissioneToFinalize = function (ordineMissione) {
        $location.path('/ordine-missione/'+ordineMissione.id+'/'+"D");
    };
    $scope.doSelectAnnullamentoOrdineMissione = function (annullamento) {
        $location.path('/annullamento-ordine-missione/'+annullamento.id);
    };
    $scope.doSelectAnnullamentoOrdineMissioneValidazione = function (annullamento) {
        $location.path('/annullamento-ordine-missione/'+annullamento.id+'/'+"S");
    };
    $scope.doSelectAnnullamentoRimborsoMissione = function (annullamento) {
        $location.path('/annullamento-rimborso-missione/'+annullamento.id);
    };
    $scope.ricercaFinita = function (){
        $scope.endSearchCmisOrdine && $scope.endSearchCmisRimborso && $scope.endSearchCmisAnnullamenti && $scope.endSearchCmisAnnullamentiRimborso;
    }
});

missioniApp.controller('AdminController', function ($scope) {
    });

missioniApp.controller('LanguageController', function ($scope, $translate, LanguageService) {
        $scope.changeLanguage = function (languageKey) {
            $translate.use(languageKey);

            LanguageService.getBy(languageKey).then(function(languages) {
                $scope.languages = languages;
            });
        };

        LanguageService.getBy().then(function (languages) {
            $scope.languages = languages;
        });
    });

missioniApp.controller('MenuController', function ($scope) {
    });

missioniApp.controller('LoginController', function ($rootScope, $scope, $location, AuthenticationSharedService, AuthServerProvider, Account) {
        $rootScope.salvataggio = true;
        $rootScope.isUserNotKeycloak = false;
        $rootScope.isUserKeycloak = false;
        AuthServerProvider.profileInfo().then(function (profile) {
                    if (profile.data.keycloakEnabled) {
                        $rootScope.isUserKeycloak = true;
                    } else {
                        $rootScope.isUserNotKeycloak = true;
                    }
            $scope.settingsAccount = Account.get(function (account){
                $rootScope.salvataggio = false;
                if (account && account.uid){
                    AuthenticationSharedService.login({
                        user: account
                    });
                }
            });
        });
        $scope.login = function () {
            AuthenticationSharedService.login({
                username: $scope.username,
                password: $scope.password
            });
        }
    });

missioniApp.controller('LogoutController', function ($location, AuthenticationSharedService) {
        AuthenticationSharedService.logout();
    });

missioniApp.controller('SettingsController', function ($rootScope, $scope, Account) {
        $scope.success = null;
        $scope.error = null;
        $rootScope.salvataggio = true;
        $scope.settingsAccount = Account.get();
        $rootScope.salvataggio = false;

        $scope.save = function () {
            $scope.success = null;
            $scope.error = null;
            $scope.errorEmailExists = null;
            Account.save($scope.settingsAccount,
                function (value, responseHeaders) {
                    $scope.error = null;
                    $scope.success = 'OK';
                    $rootScope.salvataggio = true;
                    $scope.settingsAccount = Account.get();
                    $rootScope.salvataggio = false;
                },
                function (httpResponse) {
                    if (httpResponse.status === 400 && httpResponse.data === "e-mail address already in use") {
                        $scope.errorEmailExists = "ERROR";
                    } else {
                        $scope.error = "ERROR";
                    }
                });
        };
    });

missioniApp.controller('RegisterController', function ($scope, $translate, Register) {
        $scope.success = null;
        $scope.error = null;
        $scope.doNotMatch = null;
        $scope.errorUserExists = null;
        $scope.register = function () {
            if ($scope.registerAccount.password != $scope.confirmPassword) {
                $scope.doNotMatch = "ERROR";
            } else {
                $scope.registerAccount.langKey = $translate.use();
                $scope.doNotMatch = null;
                $scope.success = null;
                $scope.error = null;
                $scope.errorUserExists = null;
                $scope.errorEmailExists = null;
                Register.save($scope.registerAccount,
                    function (value, responseHeaders) {
                        $scope.success = 'OK';
                    },
                    function (httpResponse) {
                        if (httpResponse.status === 400 && httpResponse.data === "login already in use") {
                            $scope.error = null;
                            $scope.errorUserExists = "ERROR";
                        } else if (httpResponse.status === 400 && httpResponse.data === "e-mail address already in use") {
                            $scope.error = null;
                            $scope.errorEmailExists = "ERROR";
                        } else {
                            $scope.error = "ERROR";
                        }
                    });
            }
        }
    });

missioniApp.controller('RegisterController', function ($scope, $translate, Register) {
        $scope.success = null;
        $scope.error = null;
        $scope.doNotMatch = null;
        $scope.errorUserExists = null;
        $scope.register = function () {
            if ($scope.registerAccount.password != $scope.confirmPassword) {
                $scope.doNotMatch = "ERROR";
            } else {
                $scope.registerAccount.langKey = $translate.use();
                $scope.doNotMatch = null;
                $scope.success = null;
                $scope.error = null;
                $scope.errorUserExists = null;
                $scope.errorEmailExists = null;
                Register.save($scope.registerAccount,
                    function (value, responseHeaders) {
                        $scope.success = 'OK';
                    },
                    function (httpResponse) {
                        if (httpResponse.status === 400 && httpResponse.data === "login already in use") {
                            $scope.error = null;
                            $scope.errorUserExists = "ERROR";
                        } else if (httpResponse.status === 400 && httpResponse.data === "e-mail address already in use") {
                            $scope.error = null;
                            $scope.errorEmailExists = "ERROR";
                        } else {
                            $scope.error = "ERROR";
                        }
                    });
            }
        }
    });

missioniApp.controller('ActivationController', function ($scope, $routeParams, Activate) {
        Activate.get({key: $routeParams.key},
            function (value, responseHeaders) {
                $scope.error = null;
                $scope.success = 'OK';
            },
            function (httpResponse) {
                $scope.success = null;
                $scope.error = "ERROR";
            });
    });

missioniApp.controller('PasswordController', function ($scope, Password) {
        $scope.success = null;
        $scope.error = null;
        $scope.doNotMatch = null;
        $scope.changePassword = function () {
            if ($scope.password != $scope.confirmPassword) {
                $scope.doNotMatch = "ERROR";
            } else {
                $scope.doNotMatch = null;
                Password.save($scope.password,
                    function (value, responseHeaders) {
                        $scope.error = null;
                        $scope.success = 'OK';
                    },
                    function (httpResponse) {
                        $scope.success = null;
                        $scope.error = "ERROR";
                    });
            }
        };
    });

missioniApp.controller('SessionsController', function ($scope, resolvedSessions, Sessions) {
        $scope.success = null;
        $scope.error = null;
        $scope.sessions = resolvedSessions;
        $scope.invalidate = function (series) {
            Sessions.delete({series: encodeURIComponent(series)},
                function (value, responseHeaders) {
                    $scope.error = null;
                    $scope.success = "OK";
                    $scope.sessions = Sessions.get();
                },
                function (httpResponse) {
                    $scope.success = null;
                    $scope.error = "ERROR";
                });
        };
    });

 missioniApp.controller('HealthController', function ($scope, HealthCheckService) {
     $scope.updatingHealth = true;

     $scope.refresh = function() {
         $scope.updatingHealth = true;
         HealthCheckService.check().then(function(promise) {
             $scope.healthCheck = promise;
             $scope.updatingHealth = false;
         },function(promise) {
             $scope.healthCheck = promise.data;
             $scope.updatingHealth = false;
         });
     }

     $scope.refresh();

     $scope.getLabelClass = function(statusState) {
         if (statusState == 'UP') {
             return "label-success";
         } else {
             return "label-danger";
         }
     }
 });

missioniApp.controller('ConfigurationController', function ($scope, resolvedConfiguration) {
    $scope.configuration = resolvedConfiguration;
});

missioniApp.controller('MetricsController', function ($scope, MetricsService, HealthCheckService, ThreadDumpService) {
        $scope.metrics = {};
		$scope.updatingMetrics = true;

        $scope.refresh = function() {
			$scope.updatingMetrics = true;
			MetricsService.get().then(function(promise) {
        		$scope.metrics = promise;
				$scope.updatingMetrics = false;
        	},function(promise) {
        		$scope.metrics = promise.data;
				$scope.updatingMetrics = false;
        	});
        };

		$scope.$watch('metrics', function(newValue, oldValue) {
			$scope.servicesStats = {};
            $scope.cachesStats = {};
            angular.forEach(newValue.timers, function(value, key) {
                if (key.indexOf("web.rest") != -1 || key.indexOf("service") != -1) {
                    $scope.servicesStats[key] = value;
                }

                if (key.indexOf("net.sf.ehcache.Cache") != -1) {
                    // remove gets or puts
                    var index = key.lastIndexOf(".");
                    var newKey = key.substr(0, index);

                    // Keep the name of the domain
                    index = newKey.lastIndexOf(".");
                    $scope.cachesStats[newKey] = {
                        'name': newKey.substr(index + 1),
                        'value': value
                    };
                };
            });
		});

        $scope.refresh();

        $scope.threadDump = function() {
            ThreadDumpService.dump().then(function(data) {
                $scope.threadDump = data;

                $scope.threadDumpRunnable = 0;
                $scope.threadDumpWaiting = 0;
                $scope.threadDumpTimedWaiting = 0;
                $scope.threadDumpBlocked = 0;

                angular.forEach(data, function(value, key) {
                    if (value.threadState == 'RUNNABLE') {
                        $scope.threadDumpRunnable += 1;
                    } else if (value.threadState == 'WAITING') {
                        $scope.threadDumpWaiting += 1;
                    } else if (value.threadState == 'TIMED_WAITING') {
                        $scope.threadDumpTimedWaiting += 1;
                    } else if (value.threadState == 'BLOCKED') {
                        $scope.threadDumpBlocked += 1;
                    }
                });

                $scope.threadDumpAll = $scope.threadDumpRunnable + $scope.threadDumpWaiting +
                    $scope.threadDumpTimedWaiting + $scope.threadDumpBlocked;

            });
        };

        $scope.getLabelClass = function(threadState) {
            if (threadState == 'RUNNABLE') {
                return "label-success";
            } else if (threadState == 'WAITING') {
                return "label-info";
            } else if (threadState == 'TIMED_WAITING') {
                return "label-warning";
            } else if (threadState == 'BLOCKED') {
                return "label-danger";
            }
        };
    });

missioniApp.controller('LogsController', function ($scope, resolvedLogs, LogsService) {
        $scope.loggers = resolvedLogs;

        $scope.changeLevel = function (name, level) {
            LogsService.changeLevel({name: name, level: level}, function () {
                $scope.loggers = LogsService.findAll();
            });
        }
    });

missioniApp.controller('AuditsController', function ($scope, $translate, $filter, AuditsService) {
        $scope.onChangeDate = function() {
            AuditsService.findByDates($scope.fromDate, $scope.toDate).then(function(data){
                $scope.audits = data;
            });
        };

        // Date picker configuration
        $scope.today = function() {
            // Today + 1 day - needed if the current day must be included
            var today = new Date();
            var tomorrow = new Date(today.getFullYear(), today.getMonth(), today.getDate()+1); // create new increased date

            $scope.toDate = $filter('date')(tomorrow, "yyyy-MM-dd");
        };

        $scope.previousMonth = function() {
            var fromDate = new Date();
            if (fromDate.getMonth() == 0) {
                fromDate = new Date(fromDate.getFullYear() - 1, 0, fromDate.getDate());
            } else {
                fromDate = new Date(fromDate.getFullYear(), fromDate.getMonth() - 1, fromDate.getDate());
            }

            $scope.fromDate = $filter('date')(fromDate, "yyyy-MM-dd");
        };

        $scope.today();
        $scope.previousMonth();

        AuditsService.findByDates($scope.fromDate, $scope.toDate).then(function(data){
            $scope.audits = data;
        });
    });
