document.addEventListener('DOMContentLoaded', function() {
    // Initialize Bootstrap tooltips
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltipTriggerList.forEach(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));

    // Confirmation for critical actions
    const confirmActions = document.querySelectorAll('.confirm-action');
    confirmActions.forEach(element => {
        element.addEventListener('click', function(event) {
            if (!confirm('Êtes-vous sûr de vouloir effectuer cette action ?')) {
                event.preventDefault();
            }
        });
    });

    // Form validation feedback
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        }, false);
    });

    // Table search functionality
    const searchInputs = {
        'searchLivres': 'livresTable',
        'searchAdherents': 'adherentsTable',
        'searchPrets': 'pretsTable',
        'searchReservations': 'reservationsTable',
        'searchDemandes': 'demandesTable'
    };

    Object.keys(searchInputs).forEach(inputId => {
        const input = document.getElementById(inputId);
        if (input) {
            input.addEventListener('input', function() {
                const filter = input.value.toLowerCase();
                const table = document.getElementById(searchInputs[inputId]);
                const rows = table.getElementsByTagName('tr');

                for (let i = 0; i < rows.length; i++) {
                    const cells = rows[i].getElementsByTagName('td');
                    let match = false;
                    for (let j = 0; j < cells.length; j++) {
                        if (cells[j].textContent.toLowerCase().includes(filter)) {
                            match = true;
                            break;
                        }
                    }
                    rows[i].style.display = match ? '' : 'none';
                }
            });
        }
    });
});