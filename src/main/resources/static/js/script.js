// Scripts pour améliorer l'expérience utilisateur
document.addEventListener('DOMContentLoaded', function() {
    // Ajoute des tooltips Bootstrap aux éléments avec l'attribut data-bs-toggle="tooltip"
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });

    // Confirmation avant suppression ou action critique
    const confirmActions = document.querySelectorAll('.confirm-action');
    confirmActions.forEach(function(element) {
        element.addEventListener('click', function(event) {
            if (!confirm('Êtes-vous sûr de vouloir effectuer cette action ?')) {
                event.preventDefault();
            }
        });
    });
});