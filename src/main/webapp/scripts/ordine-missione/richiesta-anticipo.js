'use strict';

missioniApp.factory('RichiestaAnticipoService', function ($resource) {
        return $resource('app/rest/ordineMissione/anticipo/:ids', {}, {
            'confirm':  { method: 'PUT', params:{confirm:true}}
        });
    });

missioniApp.controller('AnticipoOrdineMissioneController', function ($scope, $rootScope, $location, $routeParams, $sessionStorage, $http, $filter, RichiestaAnticipoService, ElencoOrdiniMissioneService, ui, COSTANTI, AccessToken) {
    
    $scope.idOrdineMissione = $routeParams.idOrdineMissione;
    $scope.accessToken = AccessToken.get();
    $scope.accountModel = $sessionStorage.accountWork;

    $scope.confirmDelete = function () {
        ui.confirmCRUD("Confermi l'eliminazione della richiesta di anticipo per l'ordine di missione numero "+$scope.anticipoOrdineMissioneModel.ordineMissione.numero+" del "+$filter('date')($scope.anticipoOrdineMissioneModel.ordineMissione.dataInserimento, COSTANTI.FORMATO_DATA)+"?", deleteAnticipo);
    }

    var inizializzaDati = function(){
            ElencoOrdiniMissioneService.findById($scope.idOrdineMissione).then(function(data){
                $scope.anticipoOrdineMissioneModel = {ordineMissione:data};
            });
    }

    $http.get('app/rest/ordineMissione/anticipo/get', {params: {idMissione: $scope.idOrdineMissione}}).then(function (response) {
        var datiAnticipoOrdineMissione = response.data;
        if (datiAnticipoOrdineMissione.id === undefined){
            inizializzaDati();
            $scope.isOrdineMissioneConfermato = false;
        } else {
            $scope.anticipoOrdineMissioneModel = datiAnticipoOrdineMissione;
            if ($scope.anticipoOrdineMissioneModel.ordineMissione.stato==='CON'){
                $scope.isOrdineMissioneConfermato = true;
            } else {
                $scope.isOrdineMissioneConfermato = false;
            }
        }
    });


    $scope.today = function() {
            // Today + 1 day - needed if the current day must be included
            var today = new Date();
            today = new Date(today.getFullYear(), today.getMonth(), today.getDate()); // create new date
            $scope.ordineMissioneModel.dataInserimento = $filter('date')(today, "dd-MM-yyyy");
    };

    $scope.save = function () {
            $rootScope.salvataggio = true;
            if ($scope.anticipoOrdineMissioneModel.id){
                $http.put('app/rest/ordineMissione/anticipo/modify', $scope.anticipoOrdineMissioneModel).success(function(data){
                    $rootScope.salvataggio = false;
                }).error(function (data) {
                    $rootScope.salvataggio = false;
                    ui.error(data);
                });
            } else {
                $http.post('app/rest/ordineMissione/anticipo/create', $scope.anticipoOrdineMissioneModel).success(function(data){
                    $rootScope.salvataggio = false;
                    $scope.anticipoOrdineMissioneModel = data;
                }).error(function (data) {
                    $rootScope.salvataggio = false;
                    ui.error(data);
                });
            }
    }

    $scope.confirm = function () {
        ui.confirmCRUD("Si sta per rendere definitiva la richiesta di anticipo di " + $scope.anticipoOrdineMissioneModel.importo+" per l'Ordine di Missione Numero: "+$scope.anticipoOrdineMissioneModel.ordineMissione.numero+" del "+$filter('date')($scope.anticipoOrdineMissioneModel.ordineMissione.dataInserimento, COSTANTI.FORMATO_DATA)+". L'operazione avvierà il processo di autorizzazione e la richiesta di anticipo non sarà più modificabile. Si desidera Continuare?", confirmAnticipo);
    }

    var confirmAnticipo = function () {
            $rootScope.salvataggio = true;
            RichiestaAnticipoService.confirm($scope.anticipoOrdineMissioneModel,
                    function (responseHeaders) {
                        $rootScope.salvataggio = false;
                    },
                    function (httpResponse) {
                        $rootScope.salvataggio = false;
                    }
            );
    }

    var deleteAnticipo = function () {
        var idAnticipo = $scope.anticipoOrdineMissioneModel.id;
            $rootScope.salvataggio = true;
            $http.delete('app/rest/ordineMissione/anticipo/' + idAnticipo).success(
                    function (data) {
                        $rootScope.salvataggio = false;
                        inizializzaDati();
                    }).error(
                    function (data) {
                        $rootScope.salvataggio = false;
                        ui.error(data);
                    }
            );
    }

    $scope.previousPage = function () {
      parent.history.back();
    }
});
