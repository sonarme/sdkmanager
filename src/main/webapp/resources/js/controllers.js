'use strict';

/* Controllers */

angular.module('dashboard.controllers', [])
    .controller('MarketingBuild', ['$scope', 'Campaign', function ($scope, Campaign) {
        $scope.predicates = ['and', 'or']
        $scope.predicateIdx = 0
        $scope.attributes = [
            {name: 'Gender', id: 'gender'},
            {name: 'Income', id: 'income'}
        ];
        $scope.compareOps = [
            {name: 'equals', id: 'EQUAL'},
            {name: 'does not equal', id: 'NOTEQUAL'},
            {name: 'less than', id: 'LESS'},
            {name: 'less than or equal', id: 'LESSOREQUAL'},
            {name: 'greater than', id: 'GREATER'},
            {name: 'greater than or equal', id: 'GREATEROREQUAL'}
        ];
        $scope.clauses = [
            {attribute: "", compareOp: ""}
        ];
        function _fn_error(err) {
            alert(err);

        }

        function _fn_success_put_post(data) {
        }

        $scope.togglePredicate = function () {
            $scope.predicateIdx = ($scope.predicateIdx + 1) % $scope.predicates.length
        }

        $scope.save = function () {
            var campaign = new Campaign($scope.campaign);
            campaign.clauses = $scope.clauses;
            campaign.$save();
        };

        $scope.createClause = function () {
            $scope.clauses.push({});
        }
    }])
    .controller('GeofenceBuildCtrl', [function () {

    }]);